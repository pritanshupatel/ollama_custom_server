package com.example.ollamaadapter;

import com.example.ollamaadapter.api.ChatCompletionsResource;
import com.example.ollamaadapter.auth.ApiKeyAuthFilter;
import com.example.ollamaadapter.core.ApiLoggingFilter;
import com.example.ollamaadapter.core.RequestIdFilter;
import com.example.ollamaadapter.dto.openai.ChatChoice;
import com.example.ollamaadapter.dto.openai.ChatCompletionResponse;
import com.example.ollamaadapter.dto.openai.ChatMessage;
import com.example.ollamaadapter.dto.openai.Usage;
import com.example.ollamaadapter.exception.ApiException;
import com.example.ollamaadapter.exception.GlobalExceptionMapper;
import com.example.ollamaadapter.service.ChatCompletionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
class ChatCompletionsResourceTest {

    private static final ChatCompletionService CHAT_SERVICE = Mockito.mock(ChatCompletionService.class);
    private static final ChatCompletionService TIMEOUT_SERVICE = Mockito.mock(ChatCompletionService.class);
    private static final ChatCompletionService MALFORMED_SERVICE = Mockito.mock(ChatCompletionService.class);
    private static final ChatCompletionService AUTH_SERVICE = Mockito.mock(ChatCompletionService.class);
    private static final ApiLoggingFilter API_LOGGING_FILTER = new ApiLoggingFilter(true, false, 20_000, new ObjectMapper());

    private static final ResourceExtension RESOURCE = ResourceExtension.builder()
        .addProvider(new RequestIdFilter())
        .addProvider(API_LOGGING_FILTER)
        .addProvider(new GlobalExceptionMapper())
        .addResource(new ChatCompletionsResource(CHAT_SERVICE))
        .build();

    private static final ResourceExtension TIMEOUT_RESOURCE = ResourceExtension.builder()
        .addProvider(new RequestIdFilter())
        .addProvider(API_LOGGING_FILTER)
        .addProvider(new GlobalExceptionMapper())
        .addResource(new ChatCompletionsResource(TIMEOUT_SERVICE))
        .build();

    private static final ResourceExtension MALFORMED_RESOURCE = ResourceExtension.builder()
        .addProvider(new RequestIdFilter())
        .addProvider(API_LOGGING_FILTER)
        .addProvider(new GlobalExceptionMapper())
        .addResource(new ChatCompletionsResource(MALFORMED_SERVICE))
        .build();

    private static final ResourceExtension AUTH_RESOURCE = ResourceExtension.builder()
        .addProvider(new RequestIdFilter())
        .addProvider(API_LOGGING_FILTER)
        .addProvider(new ApiKeyAuthFilter("secret-key"))
        .addProvider(new GlobalExceptionMapper())
        .addResource(new ChatCompletionsResource(AUTH_SERVICE))
        .build();

    @Test
    void chatCompletionsReturnsOpenAiShape() {
        ChatCompletionResponse response = new ChatCompletionResponse(
            "chatcmpl-123",
            "chat.completion",
            1L,
            "qwen3.5:9b",
            List.of(new ChatChoice(0, new ChatMessage("assistant", "Hello back"), "stop")),
            new Usage(0, 0, 0)
        );
        when(CHAT_SERVICE.createCompletion(any())).thenReturn(response);

        Response httpResponse = RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity(validRequestJson(false), MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(200, httpResponse.getStatus());
        assertTrue(body.contains("\"object\":\"chat.completion\""));
        assertTrue(body.contains("\"role\":\"assistant\""));
        assertTrue(body.contains("\"content\":\"Hello back\""));
    }

    @Test
    void streamTrueReturnsBadRequestWhenStreamingIsNotImplemented() {
        Response httpResponse = RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity(validRequestJson(true), MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(400, httpResponse.getStatus());
        assertTrue(body.contains("\"code\":\"stream_not_supported\""));
    }

    @Test
    void authEnabledMissingTokenReturnsUnauthorized() {
        Response httpResponse = AUTH_RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity(validRequestJson(false), MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(401, httpResponse.getStatus());
        assertTrue(body.contains("\"code\":\"unauthorized\""));
    }

    @Test
    void ollamaTimeoutReturnsNormalizedError() {
        when(TIMEOUT_SERVICE.createCompletion(any()))
            .thenThrow(new ApiException(504, "Ollama request timed out.", "ollama_timeout"));

        Response httpResponse = TIMEOUT_RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity(validRequestJson(false), MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(504, httpResponse.getStatus());
        assertTrue(body.contains("\"code\":\"ollama_timeout\""));
        assertTrue(body.contains("\"type\":\"adapter_error\""));
    }

    @Test
    void malformedOllamaResponseReturnsNormalizedError() {
        when(MALFORMED_SERVICE.createCompletion(any()))
            .thenThrow(new ApiException(502, "Ollama returned malformed JSON.", "invalid_upstream_response"));

        Response httpResponse = MALFORMED_RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity(validRequestJson(false), MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(502, httpResponse.getStatus());
        assertTrue(body.contains("\"code\":\"invalid_upstream_response\""));
    }

    @Test
    void invalidTemperatureReturnsClearFieldError() {
        Response httpResponse = RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity(invalidTemperatureRequestJson(), MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(422, httpResponse.getStatus());
        assertTrue(body.contains("Invalid field 'temperature'"));
        assertTrue(body.contains("\"param\":\"temperature\""));
        assertTrue(body.contains("\"code\":\"invalid_temperature\""));
    }

    @Test
    void invalidMaxTokensReturnsClearFieldError() {
        Response httpResponse = RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity(invalidMaxTokensRequestJson(), MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(422, httpResponse.getStatus());
        assertTrue(body.contains("Invalid field 'max_tokens'"));
        assertTrue(body.contains("\"param\":\"max_tokens\""));
        assertTrue(body.contains("\"code\":\"invalid_max_tokens\""));
    }

    @Test
    void invalidJsonBodyReturnsClearDtoParseError() {
        Response httpResponse = RESOURCE.target("/v1/chat/completions")
            .request()
            .post(Entity.entity("""
                {
                  "model": "qwen3.5:9b",
                  "messages": [
                    {"role": "user", "content": "Hello"}
                  ],
                """, MediaType.APPLICATION_JSON_TYPE));

        String body = httpResponse.readEntity(String.class);
        assertEquals(400, httpResponse.getStatus());
        assertTrue(body.contains("\"code\":\"invalid_json_body\""));
        assertTrue(body.contains("Invalid JSON body"));
    }

    private String validRequestJson(boolean stream) {
        return """
            {
              "model": "qwen3.5:9b",
              "messages": [
                {"role": "system", "content": "You are helpful."},
                {"role": "user", "content": "Hello"}
              ],
              "temperature": 0.1,
              "top_p": 1.0,
              "max_tokens": 300,
              "stream": %s
            }
            """.formatted(stream);
    }

    private String invalidTemperatureRequestJson() {
        return """
            {
              "model": "qwen3.5:9b",
              "messages": [
                {"role": "user", "content": "Hello"}
              ],
              "temperature": -1.0,
              "stream": false
            }
            """;
    }

    private String invalidMaxTokensRequestJson() {
        return """
            {
              "model": "qwen3.5:9b",
              "messages": [
                {"role": "user", "content": "Hello"}
              ],
              "max_tokens": 0,
              "stream": false
            }
            """;
    }
}
