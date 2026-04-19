package com.example.ollamaadapter.dto.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaTagsResponse {

    private List<OllamaModelInfo> models = new ArrayList<>();

    public OllamaTagsResponse() {
    }

    public OllamaTagsResponse(List<OllamaModelInfo> models) {
        this.models = models;
    }

    @JsonProperty
    public List<OllamaModelInfo> getModels() {
        return models;
    }

    @JsonProperty
    public void setModels(List<OllamaModelInfo> models) {
        this.models = models;
    }
}
