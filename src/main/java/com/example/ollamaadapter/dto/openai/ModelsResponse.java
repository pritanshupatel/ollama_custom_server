package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ModelsResponse {

    private String object;
    private List<ModelInfo> data = new ArrayList<>();

    public ModelsResponse() {
    }

    public ModelsResponse(String object, List<ModelInfo> data) {
        this.object = object;
        this.data = data;
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
    public List<ModelInfo> getData() {
        return data;
    }

    @JsonProperty
    public void setData(List<ModelInfo> data) {
        this.data = data;
    }
}
