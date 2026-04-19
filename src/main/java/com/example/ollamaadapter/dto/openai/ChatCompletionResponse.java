package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ChatCompletionResponse {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChatChoice> choices = new ArrayList<>();
    private Usage usage;

    public ChatCompletionResponse() {
    }

    public ChatCompletionResponse(String id, String object, long created, String model, List<ChatChoice> choices, Usage usage) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.model = model;
        this.choices = choices;
        this.usage = usage;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    public String getObject() {
        return object;
    }

    @JsonProperty
    public void setObject(String object) {
        this.object = object;
    }

    @JsonProperty
    public long getCreated() {
        return created;
    }

    @JsonProperty
    public void setCreated(long created) {
        this.created = created;
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
    public List<ChatChoice> getChoices() {
        return choices;
    }

    @JsonProperty
    public void setChoices(List<ChatChoice> choices) {
        this.choices = choices;
    }

    @JsonProperty
    public Usage getUsage() {
        return usage;
    }

    @JsonProperty
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
}
