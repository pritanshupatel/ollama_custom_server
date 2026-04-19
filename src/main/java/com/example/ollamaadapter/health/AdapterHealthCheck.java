package com.example.ollamaadapter.health;

import com.codahale.metrics.health.HealthCheck;
import com.example.ollamaadapter.client.OllamaClient;

public class AdapterHealthCheck extends HealthCheck {

    private final OllamaClient ollamaClient;

    public AdapterHealthCheck(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    @Override
    protected Result check() {
        try {
            ollamaClient.listModels();
            return Result.healthy();
        } catch (Exception e) {
            return Result.unhealthy("Ollama is unavailable: " + e.getMessage());
        }
    }
}
