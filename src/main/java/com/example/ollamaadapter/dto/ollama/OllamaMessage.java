package com.example.ollamaadapter.dto.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaMessage {

    private String role;
    private String content;

    public OllamaMessage() {
    }

    public OllamaMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    @JsonProperty
    public String getRole() {
        return role;
    }

    @JsonProperty
    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty
    public String getContent() {
        return content;
    }

    @JsonProperty
    public void setContent(String content) {
        this.content = content;
    }
}
