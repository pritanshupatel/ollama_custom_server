package com.example.ollamaadapter.client;

import com.example.ollamaadapter.dto.ollama.OllamaChatRequest;
import com.example.ollamaadapter.dto.ollama.OllamaChatResponse;
import com.example.ollamaadapter.dto.ollama.OllamaTagsResponse;

public interface OllamaClient {

    OllamaTagsResponse listModels();

    OllamaChatResponse chat(OllamaChatRequest request);
}
