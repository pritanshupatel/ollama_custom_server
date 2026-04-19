package com.example.ollamaadapter.api;

import com.example.ollamaadapter.core.RequestContext;
import com.example.ollamaadapter.dto.openai.ChatCompletionRequest;
import com.example.ollamaadapter.dto.openai.ChatCompletionResponse;
import com.example.ollamaadapter.dto.openai.ChatMessage;
import com.example.ollamaadapter.exception.ApiException;
import com.example.ollamaadapter.service.ChatCompletionService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/chat/completions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatCompletionsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatCompletionsResource.class);

    private final ChatCompletionService chatCompletionService;

    public ChatCompletionsResource(ChatCompletionService chatCompletionService) {
        this.chatCompletionService = chatCompletionService;
    }

    @POST
    public ChatCompletionResponse createChatCompletion(
        @Valid ChatCompletionRequest request,
        @HeaderParam("Content-Type") String contentType,
        @HeaderParam("Authorization") String authorizationHeader
    ) {
        logRequestSummary(request, contentType, authorizationHeader);
        validateRequest(request);

        if (request.getModel() == null || request.getModel().isBlank()) {
            LOGGER.info("chat completion request missing model; default model will be used");
        }

        if (Boolean.TRUE.equals(request.getStream())) {
            LOGGER.warn("chat completion request rejected because stream=true is not supported");
            throw new ApiException(
                400,
                "Streaming is not implemented for this adapter. Send stream=false.",
                "adapter_error",
                "stream",
                "stream_not_supported",
                null
            );
        }
        RequestContext.setModel(request.getModel());
        return chatCompletionService.createCompletion(request);
    }

    private void validateRequest(ChatCompletionRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            LOGGER.warn("chat completion validation failed: messages is missing or empty");
            throw new ApiException(422, "Invalid field 'messages': at least one message is required.", "adapter_error", "messages", "invalid_messages", null);
        }

        for (int index = 0; index < request.getMessages().size(); index++) {
            ChatMessage message = request.getMessages().get(index);
            if (message == null) {
                LOGGER.warn("chat completion validation failed: messages[{}] is null", index);
                throw new ApiException(422, "Invalid field 'messages[" + index + "]': message entry must not be null.", "adapter_error", "messages[" + index + "]", "invalid_messages", null);
            }
            LOGGER.debug("chat completion message parsed index={} role={} contentKind={}", index, message.getRole(), message.getContentKind());
            if (message.getContent() == null) {
                LOGGER.warn("chat completion validation failed: messages[{}].content is null", index);
                throw new ApiException(422, "Invalid field 'messages[" + index + "].content': content must not be null.", "adapter_error", "messages[" + index + "].content", "invalid_message_content", null);
            }
        }

        if (request.getTemperature() != null && (Double.isNaN(request.getTemperature()) || Double.isInfinite(request.getTemperature()) || request.getTemperature() < 0.0d || request.getTemperature() > 2.0d)) {
            LOGGER.warn("chat completion validation failed: temperature={} is outside the supported range", request.getTemperature());
            throw new ApiException(422, "Invalid field 'temperature': must be between 0 and 2.", "adapter_error", "temperature", "invalid_temperature", null);
        }

        if (request.getTopP() != null && (Double.isNaN(request.getTopP()) || Double.isInfinite(request.getTopP()) || request.getTopP() <= 0.0d || request.getTopP() > 1.0d)) {
            LOGGER.warn("chat completion validation failed: top_p={} is outside the supported range", request.getTopP());
            throw new ApiException(422, "Invalid field 'top_p': must be greater than 0 and less than or equal to 1.", "adapter_error", "top_p", "invalid_top_p", null);
        }

        if (request.getMaxTokens() != null && request.getMaxTokens() <= 0) {
            LOGGER.warn("chat completion validation failed: max_tokens={} must be greater than zero", request.getMaxTokens());
            throw new ApiException(422, "Invalid field 'max_tokens': must be greater than 0.", "adapter_error", "max_tokens", "invalid_max_tokens", null);
        }
    }

    private void logRequestSummary(ChatCompletionRequest request, String contentType, String authorizationHeader) {
        boolean modelPresent = request.getModel() != null && !request.getModel().isBlank();
        int messagesCount = request.getMessages() == null ? 0 : request.getMessages().size();
        boolean authHeaderPresent = authorizationHeader != null && !authorizationHeader.isBlank();

        LOGGER.debug(
            "chat request summary modelPresent={} messagesCount={} stream={} temperature={} topP={} maxTokens={} contentType={} authHeaderPresent={}",
            modelPresent,
            messagesCount,
            request.getStream(),
            valueOrUnset(request.getTemperature()),
            valueOrUnset(request.getTopP()),
            valueOrUnset(request.getMaxTokens()),
            valueOrUnset(contentType),
            authHeaderPresent
        );
    }

    private String valueOrUnset(Object value) {
        return value == null ? "unset" : String.valueOf(value);
    }
}
