package com.example.ollamaadapter.service;

import com.example.ollamaadapter.client.OllamaClient;
import com.example.ollamaadapter.core.ConcurrencyGuard;
import com.example.ollamaadapter.core.RequestContext;
import com.example.ollamaadapter.dto.ollama.OllamaChatRequest;
import com.example.ollamaadapter.dto.ollama.OllamaChatResponse;
import com.example.ollamaadapter.dto.openai.ChatCompletionRequest;
import com.example.ollamaadapter.dto.openai.ChatCompletionResponse;

public class ChatCompletionService {

    private final OllamaClient ollamaClient;
    private final ResponseMapper responseMapper;
    private final ConcurrencyGuard concurrencyGuard;
    private final String defaultModel;

    public ChatCompletionService(OllamaClient ollamaClient, ResponseMapper responseMapper, ConcurrencyGuard concurrencyGuard, String defaultModel) {
        this.ollamaClient = ollamaClient;
        this.responseMapper = responseMapper;
        this.concurrencyGuard = concurrencyGuard;
        this.defaultModel = defaultModel;
    }

    public ChatCompletionResponse createCompletion(ChatCompletionRequest request) {
        try (ConcurrencyGuard.GuardHandle ignored = concurrencyGuard.acquire()) {
            String model = resolveModel(request.getModel());
            RequestContext.setModel(model);
            OllamaChatRequest ollamaRequest = responseMapper.toOllamaRequest(request, model);
            OllamaChatResponse ollamaResponse = ollamaClient.chat(ollamaRequest);
            return responseMapper.toOpenAiResponse(ollamaResponse, model);
        }
    }

    private String resolveModel(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank()) {
            return defaultModel;
        }
        return requestedModel;
    }
}
