package com.example.ollamaadapter.service;

import com.example.ollamaadapter.dto.ollama.OllamaChatRequest;
import com.example.ollamaadapter.dto.ollama.OllamaChatResponse;
import com.example.ollamaadapter.dto.ollama.OllamaMessage;
import com.example.ollamaadapter.dto.openai.ChatChoice;
import com.example.ollamaadapter.dto.openai.ChatCompletionRequest;
import com.example.ollamaadapter.dto.openai.ChatCompletionResponse;
import com.example.ollamaadapter.dto.openai.ChatMessage;
import com.example.ollamaadapter.dto.openai.Usage;
import com.example.ollamaadapter.exception.ApiException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResponseMapper {

    private final String defaultModel;
    private final OpenAiMessageNormalizer openAiMessageNormalizer;

    public ResponseMapper(String defaultModel, OpenAiMessageNormalizer openAiMessageNormalizer) {
        this.defaultModel = defaultModel;
        this.openAiMessageNormalizer = openAiMessageNormalizer;
    }

    public OllamaChatRequest toOllamaRequest(ChatCompletionRequest request, String resolvedModel) {
        List<OllamaMessage> messages = new ArrayList<>();
        for (int index = 0; index < request.getMessages().size(); index++) {
            ChatMessage message = request.getMessages().get(index);
            String normalizedContent = openAiMessageNormalizer.normalizeContent(message, index);
            messages.add(new OllamaMessage(message.getRole(), normalizedContent));
        }

        Map<String, Object> options = new LinkedHashMap<>();
        if (request.getTemperature() != null) {
            options.put("temperature", request.getTemperature());
        }
        if (request.getTopP() != null) {
            options.put("top_p", request.getTopP());
        }
        if (request.getMaxTokens() != null) {
            options.put("num_predict", request.getMaxTokens());
        }

        return new OllamaChatRequest(
            resolvedModel == null || resolvedModel.isBlank() ? defaultModel : resolvedModel,
            messages,
            false,
            options.isEmpty() ? null : options
        );
    }

    public ChatCompletionResponse toOpenAiResponse(OllamaChatResponse ollamaResponse, String requestedModel) {
        if (ollamaResponse == null || ollamaResponse.getMessage() == null) {
            throw new ApiException(502, "Ollama response did not include a message.", "invalid_upstream_response");
        }

        String content = ollamaResponse.getMessage().getContent();
        if (content == null) {
            throw new ApiException(502, "Ollama response did not include assistant content.", "invalid_upstream_response");
        }

        String resolvedModel = requestedModel;
        if (resolvedModel == null || resolvedModel.isBlank()) {
            resolvedModel = ollamaResponse.getModel();
        }
        if (resolvedModel == null || resolvedModel.isBlank()) {
            resolvedModel = defaultModel;
        }

        ChatMessage assistantMessage = new ChatMessage("assistant", content);
        ChatChoice choice = new ChatChoice(
            0,
            assistantMessage,
            normalizeFinishReason(ollamaResponse.getDoneReason())
        );

        int promptTokens = ollamaResponse.getPromptEvalCount() == null ? 0 : ollamaResponse.getPromptEvalCount();
        int completionTokens = ollamaResponse.getEvalCount() == null ? 0 : ollamaResponse.getEvalCount();

        return new ChatCompletionResponse(
            "chatcmpl-" + UUID.randomUUID().toString().replace("-", ""),
            "chat.completion",
            Instant.now().getEpochSecond(),
            resolvedModel,
            List.of(choice),
            new Usage(promptTokens, completionTokens, promptTokens + completionTokens)
        );
    }

    private String normalizeFinishReason(String doneReason) {
        if (doneReason == null || doneReason.isBlank()) {
            return "stop";
        }
        return doneReason;
    }
}
