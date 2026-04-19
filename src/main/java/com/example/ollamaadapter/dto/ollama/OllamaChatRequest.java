package com.example.ollamaadapter.dto.ollama;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class OllamaChatRequest {

    private String model;
    private List<OllamaMessage> messages;
    private boolean stream;
    private Map<String, Object> options;

    public OllamaChatRequest() {
    }

    public OllamaChatRequest(String model, List<OllamaMessage> messages, boolean stream, Map<String, Object> options) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
        this.options = options;
    }

    @JsonProperty
    public String getModel() {
        return model;
    }

    @JsonProperty
    public void setModel(String model) {
        this.model = model;
    }

    @JsonProperty
    public List<OllamaMessage> getMessages() {
        return messages;
    }

    @JsonProperty
    public void setMessages(List<OllamaMessage> messages) {
        this.messages = messages;
    }

    @JsonProperty
    public boolean isStream() {
        return stream;
    }

    @JsonProperty
    public void setStream(boolean stream) {
        this.stream = stream;
    }

    @JsonProperty
    public Map<String, Object> getOptions() {
        return options;
    }

    @JsonProperty
    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
