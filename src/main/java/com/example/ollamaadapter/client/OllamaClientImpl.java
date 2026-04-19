package com.example.ollamaadapter.client;

import com.example.ollamaadapter.core.RequestContext;
import com.example.ollamaadapter.dto.ollama.OllamaChatRequest;
import com.example.ollamaadapter.dto.ollama.OllamaChatResponse;
import com.example.ollamaadapter.dto.ollama.OllamaTagsResponse;
import com.example.ollamaadapter.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

public class OllamaClientImpl implements OllamaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaClientImpl.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String ollamaBaseUrl;
    private final Duration requestTimeout;

    public OllamaClientImpl(HttpClient httpClient, ObjectMapper objectMapper, String ollamaBaseUrl, Duration requestTimeout) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.ollamaBaseUrl = normalizeBaseUrl(ollamaBaseUrl);
        this.requestTimeout = requestTimeout;
    }

    @Override
    public OllamaTagsResponse listModels() {
        LOGGER.info("outgoing Ollama call operation=list_models url={}", ollamaBaseUrl + "/api/tags");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ollamaBaseUrl + "/api/tags"))
            .timeout(requestTimeout)
            .GET()
            .header("Accept", "application/json")
            .build();

        return send(request, OllamaTagsResponse.class, "list_models");
    }

    @Override
    public OllamaChatResponse chat(OllamaChatRequest requestBody) {
        LOGGER.info("outgoing Ollama call operation=chat model={} stream={}", requestBody.getModel(), requestBody.isStream());
        String payload;
        try {
            payload = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            LOGGER.error("failed to serialize Ollama request", e);
            throw new ApiException(500, "Failed to serialize Ollama request.", "serialization_error", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ollamaBaseUrl + "/api/chat"))
            .timeout(requestTimeout)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        return send(request, OllamaChatResponse.class, "chat");
    }

    private <T> T send(HttpRequest request, Class<T> responseType, String operation) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.error("Ollama returned non-success status operation={} status={}", operation, response.statusCode());
                RequestContext.setErrorSummary("ollama_http_" + response.statusCode());
                throw new ApiException(502, "Ollama returned an unexpected HTTP status.", "ollama_bad_gateway");
            }

            T body = objectMapper.readValue(response.body(), responseType);
            if (body == null) {
                RequestContext.setErrorSummary("ollama_empty_response");
                throw new ApiException(502, "Ollama returned an empty response.", "invalid_upstream_response");
            }
            LOGGER.info("Ollama response received operation={} status={} responseType={}", operation, response.statusCode(), responseType.getSimpleName());
            return body;
        } catch (HttpTimeoutException e) {
            RequestContext.setErrorSummary("ollama_timeout");
            LOGGER.error("timeout while calling Ollama operation={}", operation, e);
            throw new ApiException(504, "Ollama request timed out.", "ollama_timeout", e);
        } catch (ConnectException e) {
            RequestContext.setErrorSummary("ollama_connect_error");
            LOGGER.error("unable to connect to Ollama operation={}", operation, e);
            throw new ApiException(502, "Unable to connect to Ollama.", "ollama_unavailable", e);
        } catch (JsonProcessingException e) {
            RequestContext.setErrorSummary("ollama_malformed_response");
            LOGGER.error("malformed Ollama response operation={}", operation, e);
            throw new ApiException(502, "Ollama returned malformed JSON.", "invalid_upstream_response", e);
        } catch (IOException e) {
            RequestContext.setErrorSummary("ollama_io_error");
            LOGGER.error("I/O failure while calling Ollama operation={}", operation, e);
            throw new ApiException(502, "Ollama request failed.", "ollama_unavailable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            RequestContext.setErrorSummary("ollama_interrupted");
            LOGGER.error("Ollama request interrupted operation={}", operation, e);
            throw new ApiException(502, "Ollama request was interrupted.", "ollama_unavailable", e);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = baseUrl == null ? "" : baseUrl.trim();
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
