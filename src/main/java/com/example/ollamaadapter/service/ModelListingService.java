package com.example.ollamaadapter.service;

import com.example.ollamaadapter.client.OllamaClient;
import com.example.ollamaadapter.dto.ollama.OllamaModelInfo;
import com.example.ollamaadapter.dto.ollama.OllamaTagsResponse;
import com.example.ollamaadapter.dto.openai.ModelInfo;
import com.example.ollamaadapter.dto.openai.ModelsResponse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ModelListingService {

    private final OllamaClient ollamaClient;
    private final String defaultModel;

    public ModelListingService(OllamaClient ollamaClient, String defaultModel) {
        this.ollamaClient = ollamaClient;
        this.defaultModel = defaultModel;
    }

    public ModelsResponse listModels() {
        Set<String> modelIds = new LinkedHashSet<>();
        try {
            OllamaTagsResponse response = ollamaClient.listModels();
            if (response != null && response.getModels() != null) {
                for (OllamaModelInfo modelInfo : response.getModels()) {
                    if (modelInfo != null && modelInfo.getName() != null && !modelInfo.getName().isBlank()) {
                        modelIds.add(modelInfo.getName());
                    }
                }
            }
        } catch (RuntimeException ignored) {
            modelIds.add(defaultModel);
        }

        if (modelIds.isEmpty()) {
            modelIds.add(defaultModel);
        }

        List<ModelInfo> data = new ArrayList<>();
        for (String modelId : modelIds) {
            data.add(new ModelInfo(modelId, "model", 0L, "ollama"));
        }
        return new ModelsResponse("list", data);
    }
}
