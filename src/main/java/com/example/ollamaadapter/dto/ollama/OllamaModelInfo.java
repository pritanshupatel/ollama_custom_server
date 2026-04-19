package com.example.ollamaadapter.dto.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaModelInfo {

    private String name;

    public OllamaModelInfo() {
    }

    public OllamaModelInfo(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }
}
