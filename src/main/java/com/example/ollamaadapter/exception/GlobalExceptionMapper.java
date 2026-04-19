package com.example.ollamaadapter.exception;

import com.example.ollamaadapter.core.RequestContext;
import com.example.ollamaadapter.dto.openai.ErrorBody;
import com.example.ollamaadapter.dto.openai.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ApiException apiException) {
            RequestContext.setErrorSummary(apiException.getCode());
            RequestContext.setFailureCategory(classifyFailureCategory(apiException.getCode()));
            Throwable rootCause = findRootCause(apiException);
            LOGGER.error(
                "handled API exception requestId={} status={} code={} message={} rootCauseClass={} rootCauseMessage={}",
                RequestContext.getRequestId(),
                apiException.getStatus(),
                apiException.getCode(),
                apiException.getMessage(),
                rootCause.getClass().getName(),
                sanitize(rootCause.getMessage()),
                apiException
            );
            return build(apiException.getStatus(), apiException.getMessage(), apiException.getType(), apiException.getParam(), apiException.getCode());
        }

        if (exception instanceof ConstraintViolationException violationException) {
            String message = violationException.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
            RequestContext.setErrorSummary("validation_error");
            RequestContext.setFailureCategory("validation_failure");
            Throwable rootCause = findRootCause(violationException);
            LOGGER.error(
                "validation failure requestId={} message={} rootCauseClass={} rootCauseMessage={}",
                RequestContext.getRequestId(),
                message,
                rootCause.getClass().getName(),
                sanitize(rootCause.getMessage()),
                violationException
            );
            return build(422, message, "adapter_error", null, "validation_error");
        }

        Throwable rootCause = findRootCause(exception);
        if (rootCause instanceof JsonProcessingException) {
            String message = "Invalid JSON body: " + sanitize(rootCause.getMessage());
            RequestContext.setErrorSummary("invalid_json_body");
            RequestContext.setFailureCategory("dto_parse_failure");
            LOGGER.error(
                "invalid JSON body requestId={} rootCauseClass={} rootCauseMessage={}",
                RequestContext.getRequestId(),
                rootCause.getClass().getName(),
                sanitize(rootCause.getMessage()),
                exception
            );
            return build(400, message, "adapter_error", null, "invalid_json_body");
        }

        if (exception instanceof WebApplicationException webApplicationException) {
            int status = webApplicationException.getResponse() == null ? 400 : webApplicationException.getResponse().getStatus();
            String message = webApplicationException.getMessage() == null ? "Request failed." : webApplicationException.getMessage();
            String code = status == 401 ? "unauthorized" : "request_error";
            RequestContext.setErrorSummary(code);
            RequestContext.setFailureCategory(classifyFailureCategory(code));
            LOGGER.error(
                "web application exception requestId={} status={} message={} rootCauseClass={} rootCauseMessage={}",
                RequestContext.getRequestId(),
                status,
                message,
                rootCause.getClass().getName(),
                sanitize(rootCause.getMessage()),
                webApplicationException
            );
            return build(status, message, "adapter_error", null, code);
        }

        RequestContext.setFailureCategory("internal_exception");
        LOGGER.error(
            "unknown internal exception requestId={} rootCauseClass={} rootCauseMessage={}",
            RequestContext.getRequestId(),
            rootCause.getClass().getName(),
            sanitize(rootCause.getMessage()),
            exception
        );
        RequestContext.setErrorSummary("internal_error");
        return build(500, "Internal server error.", "adapter_error", null, "internal_error");
    }

    private Response build(int status, String message, String type, String param, String code) {
        ErrorResponse errorResponse = new ErrorResponse(new ErrorBody(message, type, param, code));
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(errorResponse)
            .build();
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() == null ? "request" : violation.getPropertyPath().toString();
        return path + " " + violation.getMessage();
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String classifyFailureCategory(String code) {
        if (code == null || code.isBlank()) {
            return "unknown_failure";
        }
        if ("unauthorized".equals(code)) {
            return "auth_failure";
        }
        if ("validation_error".equals(code)
            || "invalid_messages".equals(code)
            || "invalid_temperature".equals(code)
            || "invalid_top_p".equals(code)
            || "invalid_max_tokens".equals(code)
            || "stream_not_supported".equals(code)) {
            return "validation_failure";
        }
        if ("invalid_json_body".equals(code) || "request_error".equals(code)) {
            return "dto_parse_failure";
        }
        if ("ollama_timeout".equals(code)
            || "ollama_unavailable".equals(code)
            || "ollama_bad_gateway".equals(code)
            || "invalid_upstream_response".equals(code)) {
            return "downstream_ollama_failure";
        }
        if ("internal_error".equals(code)) {
            return "internal_exception";
        }
        return "request_failure";
    }

    private String sanitize(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
