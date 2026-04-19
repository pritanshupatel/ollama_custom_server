package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionRequest {

    private String model;

    @Valid
    @NotEmpty(message = "messages must not be empty")
    private List<ChatMessage> messages = new ArrayList<>();

    private Double temperature;
    private Double topP;
    private Integer maxTokens;
    private Boolean stream = Boolean.FALSE;

    public ChatCompletionRequest() {
    }

    public ChatCompletionRequest(String model, List<ChatMessage> messages, Double temperature, Double topP, Integer maxTokens, Boolean stream) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
        this.stream = stream;
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
    public List<ChatMessage> getMessages() {
        return messages;
    }

    @JsonProperty
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @JsonProperty
    public Double getTemperature() {
        return temperature;
    }

    @JsonProperty
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @JsonProperty("top_p")
    public Double getTopP() {
        return topP;
    }

    @JsonProperty("top_p")
    public void setTopP(Double topP) {
        this.topP = topP;
    }

    @JsonProperty("max_tokens")
    public Integer getMaxTokens() {
        return maxTokens;
    }

    @JsonProperty("max_tokens")
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    @JsonProperty
    public Boolean getStream() {
        return stream;
    }

    @JsonProperty
    public void setStream(Boolean stream) {
        this.stream = stream;
    }
}
