package com.example.ollamaadapter.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ollamaadapter.dto.openai.ErrorBody;
import com.example.ollamaadapter.dto.openai.ErrorResponse;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Provider
@Priority(Priorities.USER)
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiLoggingFilter.class);
    private static final Set<String> CHAT_COMPLETION_FIELDS = Set.of("model", "messages", "temperature", "top_p", "max_tokens", "stream");
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_X_API_KEY = "X-API-Key";

    private final boolean debugRequestResponseLogging;
    private final boolean debugLogRawAuthHeaders;
    private final int maxLoggedBodyChars;
    private final ObjectMapper objectMapper;

    public ApiLoggingFilter(boolean debugRequestResponseLogging, boolean debugLogRawAuthHeaders, int maxLoggedBodyChars, ObjectMapper objectMapper) {
        this.debugRequestResponseLogging = debugRequestResponseLogging;
        this.debugLogRawAuthHeaders = debugLogRawAuthHeaders;
        this.maxLoggedBodyChars = maxLoggedBodyChars;
        this.objectMapper = objectMapper;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!debugRequestResponseLogging) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        String query = requestContext.getUriInfo().getRequestUri().getRawQuery();
        String contentType = requestContext.getHeaderString("Content-Type");
        String userAgent = requestContext.getHeaderString("User-Agent");
        String authorizationHeader = requestContext.getHeaderString(HEADER_AUTHORIZATION);
        String clientAddress = resolveClientAddress(requestContext);
        String rawBody = readAndRestoreEntity(requestContext);
        JsonProcessingException jsonParseException = parseJsonException(rawBody, contentType);
        if (jsonParseException != null) {
            String message = "Invalid JSON body: " + sanitize(jsonParseException.getOriginalMessage());
            RequestContext.setErrorSummary("invalid_json_body");
            RequestContext.setFailureCategory("dto_parse_failure");
            RequestContext.setResponseStatus(400);
            RequestContext.setResponseHeaders("{Content-Type=[application/json]}");
            RequestContext.setResponseBody(truncateForLog(message));
            LOGGER.error(
                "invalid JSON body requestId={} reason={}",
                RequestContext.getRequestId(),
                sanitize(jsonParseException.getOriginalMessage()),
                jsonParseException
            );
            requestContext.abortWith(
                Response.status(400)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorResponse(new ErrorBody(message, "adapter_error", null, "invalid_json_body")))
                    .build()
            );
            return;
        }
        String prettyBody = prettifyBodyIfJson(rawBody, contentType);
        String parsedSummary = summarizeRequest(path, rawBody, contentType);

        RequestContext.setFailureCategory("success");
        RequestContext.setRequestBody(truncateForLog(prettyBody));
        RequestContext.setRequestHeaders(formatHeaders(requestContext));
        RequestContext.setRequestPath(path);

        LOGGER.info(
            "incoming request requestId={} timestamp={} method={} path=\"{}\" query=\"{}\" clientAddress=\"{}\" contentType=\"{}\" userAgent=\"{}\" authHeaderPresent={} authorization=\"{}\" headers={} rawBody={} parsedSummary={}",
            RequestContext.getRequestId(),
            Instant.now(),
            requestContext.getMethod(),
            path,
            sanitize(query),
            sanitize(clientAddress),
            sanitize(contentType),
            sanitize(userAgent),
            authorizationHeader != null && !authorizationHeader.isBlank(),
            maskHeaderValue(HEADER_AUTHORIZATION, authorizationHeader),
            RequestContext.getRequestHeaders(),
            RequestContext.getRequestBody(),
            sanitize(parsedSummary)
        );
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (!debugRequestResponseLogging) {
            cleanupRequestContext();
            return;
        }

        RequestContext.setResponseStatus(responseContext.getStatus());
        RequestContext.setResponseHeaders(formatHeaders(responseContext));
        if (!responseContext.hasEntity()) {
            logResponse(requestContext, "");
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (!debugRequestResponseLogging) {
            context.proceed();
            return;
        }

        OutputStream originalOutputStream = context.getOutputStream();
        ByteArrayOutputStream captureStream = new ByteArrayOutputStream();
        context.setOutputStream(new TeeOutputStream(originalOutputStream, captureStream));

        try {
            context.proceed();
        } finally {
            context.setOutputStream(originalOutputStream);
            RequestContext.setResponseBody(truncateForLog(captureStream.toString(StandardCharsets.UTF_8)));
            logResponse(null, RequestContext.getResponseBody());
        }
    }

    private void logResponse(ContainerRequestContext requestContext, String body) {
        if (!debugRequestResponseLogging || RequestContext.isResponseLogged()) {
            return;
        }

        long latencyMs = System.currentTimeMillis() - RequestContext.getStartTimeMillis();
        String responseBody = body == null || body.isBlank() ? "-" : body;
        String responseSource = RequestContext.getFailureCategory() == null ? "success" : RequestContext.getFailureCategory();

        LOGGER.info(
            "outgoing response requestId={} statusCode={} latencyMs={} responseSource={} headers={} body={}",
            RequestContext.getRequestId(),
            RequestContext.getResponseStatus(),
            latencyMs,
            responseSource,
            sanitize(RequestContext.getResponseHeaders()),
            responseBody
        );
        RequestContext.setResponseLogged(true);
        cleanupRequestContext();
    }

    private String readAndRestoreEntity(ContainerRequestContext requestContext) throws IOException {
        if (!requestContext.hasEntity()) {
            return "-";
        }

        byte[] bodyBytes = requestContext.getEntityStream().readAllBytes();
        requestContext.setEntityStream(new ByteArrayInputStream(bodyBytes));
        if (bodyBytes.length == 0) {
            return "-";
        }
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }

    private String prettifyBodyIfJson(String rawBody, String contentType) {
        if (rawBody == null || rawBody.isBlank() || "-".equals(rawBody)) {
            return "-";
        }
        if (contentType == null || !contentType.toLowerCase().contains("json")) {
            return rawBody;
        }
        try {
            Object parsed = objectMapper.readValue(rawBody, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
        } catch (JsonProcessingException e) {
            LOGGER.warn("invalid JSON body detected before resource parsing requestId={} reason={}", RequestContext.getRequestId(), sanitize(e.getOriginalMessage()));
            return rawBody;
        }
    }

    private JsonProcessingException parseJsonException(String rawBody, String contentType) {
        if (rawBody == null || rawBody.isBlank() || "-".equals(rawBody)) {
            return null;
        }
        if (contentType == null || !contentType.toLowerCase().contains("json")) {
            return null;
        }
        try {
            objectMapper.readTree(rawBody);
            return null;
        } catch (JsonProcessingException e) {
            return e;
        }
    }

    private String summarizeRequest(String path, String rawBody, String contentType) {
        if (rawBody == null || rawBody.isBlank() || "-".equals(rawBody)) {
            return "-";
        }
        if (contentType == null || !contentType.toLowerCase().contains("json")) {
            return "-";
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(rawBody);
            if ("v1/chat/completions".equals(path)) {
                boolean modelPresent = jsonNode.hasNonNull("model") && !jsonNode.get("model").asText().isBlank();
                int messagesCount = jsonNode.has("messages") && jsonNode.get("messages").isArray() ? jsonNode.get("messages").size() : 0;
                String stream = jsonNode.has("stream") ? jsonNode.get("stream").toString() : "unset";
                String temperature = jsonNode.has("temperature") ? jsonNode.get("temperature").toString() : "unset";
                String topP = jsonNode.has("top_p") ? jsonNode.get("top_p").toString() : "unset";
                String maxTokens = jsonNode.has("max_tokens") ? jsonNode.get("max_tokens").toString() : "unset";
                List<String> unknownFields = findUnknownFields(jsonNode, CHAT_COMPLETION_FIELDS);
                return String.format(
                    "chatCompletionSummary{modelPresent=%s,messagesCount=%d,stream=%s,temperature=%s,top_p=%s,max_tokens=%s,unknownFields=%s}",
                    modelPresent,
                    messagesCount,
                    stream,
                    temperature,
                    topP,
                    maxTokens,
                    unknownFields
                );
            }
            return "jsonRequest";
        } catch (JsonProcessingException e) {
            return "jsonParseFailed{" + sanitize(e.getOriginalMessage()) + "}";
        }
    }

    private List<String> findUnknownFields(JsonNode jsonNode, Set<String> knownFields) {
        List<String> unknownFields = new ArrayList<>();
        Iterator<String> iterator = jsonNode.fieldNames();
        while (iterator.hasNext()) {
            String field = iterator.next();
            if (!knownFields.contains(field)) {
                unknownFields.add(field);
            }
        }
        return unknownFields;
    }

    private String formatHeaders(ContainerRequestContext requestContext) {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        requestContext.getHeaders().forEach((name, values) -> headers.put(name, maskHeaderValues(name, values)));
        return headers.toString();
    }

    private String formatHeaders(ContainerResponseContext responseContext) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        responseContext.getHeaders().forEach((name, values) -> {
            List<String> stringValues = new ArrayList<>();
            for (Object value : values) {
                stringValues.add(value == null ? "null" : value.toString());
            }
            headers.put(name, stringValues);
        });
        return headers.toString();
    }

    private List<String> maskHeaderValues(String headerName, List<String> values) {
        List<String> maskedValues = new ArrayList<>();
        for (String value : values) {
            maskedValues.add(maskHeaderValue(headerName, value));
        }
        return maskedValues;
    }

    private String maskHeaderValue(String headerName, String value) {
        if (value == null) {
            return "null";
        }
        if (debugLogRawAuthHeaders) {
            return value;
        }
        if (HEADER_AUTHORIZATION.equalsIgnoreCase(headerName) || HEADER_X_API_KEY.equalsIgnoreCase(headerName)) {
            return maskSecret(value);
        }
        return value;
    }

    private String maskSecret(String value) {
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    private String resolveClientAddress(ContainerRequestContext requestContext) {
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor;
        }
        String realIp = requestContext.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return "-";
    }

    private String truncateForLog(String value) {
        if (value == null) {
            return "-";
        }
        if (value.length() <= maxLoggedBodyChars) {
            return value;
        }
        return value.substring(0, maxLoggedBodyChars) + "... [truncated at " + maxLoggedBodyChars + " chars]";
    }

    private String sanitize(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void cleanupRequestContext() {
        MDC.remove("requestId");
        RequestContext.clear();
    }

    private static final class TeeOutputStream extends OutputStream {
        private final OutputStream delegate;
        private final OutputStream branch;

        private TeeOutputStream(OutputStream delegate, OutputStream branch) {
            this.delegate = delegate;
            this.branch = branch;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            branch.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
            branch.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            branch.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
            branch.flush();
        }
    }
}
