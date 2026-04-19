package com.example.ollamaadapter.service;

import com.example.ollamaadapter.dto.openai.ChatMessage;
import com.example.ollamaadapter.dto.openai.OpenAiContentPart;
import com.example.ollamaadapter.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenAiMessageNormalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiMessageNormalizer.class);

    private final ObjectMapper objectMapper;

    public OpenAiMessageNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String normalizeContent(ChatMessage message, int index) {
        if (message == null) {
            throw new ApiException(422, "Invalid field 'messages[" + index + "]': message entry must not be null.", "adapter_error", "messages[" + index + "]", "invalid_messages", null);
        }

        Object content = message.getContent();
        if (content == null) {
            throw new ApiException(422, "Invalid field 'messages[" + index + "].content': content must not be null.", "adapter_error", "messages[" + index + "].content", "invalid_message_content", null);
        }

        if (content instanceof String stringContent) {
            LOGGER.debug("normalized message content role={} index={} contentKind=string", message.getRole(), index);
            return stringContent;
        }

        if (content instanceof List<?> rawParts) {
            LOGGER.debug("normalized message content role={} index={} contentKind=array partsCount={}", message.getRole(), index, rawParts.size());
            return normalizeContentParts(rawParts, index);
        }

        LOGGER.warn("invalid message content kind role={} index={} contentKind={}", message.getRole(), index, message.getContentKind());
        throw new ApiException(
            422,
            "Invalid field 'messages[" + index + "].content': expected a string or array of content parts.",
            "adapter_error",
            "messages[" + index + "].content",
            "invalid_message_content",
            null
        );
    }

    private String normalizeContentParts(List<?> rawParts, int index) {
        List<String> textParts = new ArrayList<>();

        for (Object rawPart : rawParts) {
            if (rawPart == null) {
                continue;
            }

            OpenAiContentPart contentPart = convertPart(rawPart, index);
            if ("text".equals(contentPart.getType()) && contentPart.getText() != null) {
                textParts.add(contentPart.getText());
            }
        }

        return String.join("\n", textParts);
    }

    private OpenAiContentPart convertPart(Object rawPart, int index) {
        if (rawPart instanceof OpenAiContentPart contentPart) {
            return contentPart;
        }

        if (rawPart instanceof Map<?, ?>) {
            return objectMapper.convertValue(rawPart, OpenAiContentPart.class);
        }

        LOGGER.warn("invalid content part structure at messages[{}].content partType={}", index, rawPart.getClass().getName());
        throw new ApiException(
            422,
            "Invalid field 'messages[" + index + "].content': content parts must be objects.",
            "adapter_error",
            "messages[" + index + "].content",
            "invalid_message_content",
            null
        );
    }
}
