package com.example.ollamaadapter.dto.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaChatResponse {

    private String model;
    private OllamaMessage message;
    private String doneReason;
    private Integer promptEvalCount;
    private Integer evalCount;

    public OllamaChatResponse() {
    }

    public OllamaChatResponse(String model, OllamaMessage message, String doneReason, Integer promptEvalCount, Integer evalCount) {
        this.model = model;
        this.message = message;
        this.doneReason = doneReason;
        this.promptEvalCount = promptEvalCount;
        this.evalCount = evalCount;
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
    public OllamaMessage getMessage() {
        return message;
    }

    @JsonProperty
    public void setMessage(OllamaMessage message) {
        this.message = message;
    }

    @JsonProperty("done_reason")
    public String getDoneReason() {
        return doneReason;
    }

    @JsonProperty("done_reason")
    public void setDoneReason(String doneReason) {
        this.doneReason = doneReason;
    }

    @JsonProperty("prompt_eval_count")
    public Integer getPromptEvalCount() {
        return promptEvalCount;
    }

    @JsonProperty("prompt_eval_count")
    public void setPromptEvalCount(Integer promptEvalCount) {
        this.promptEvalCount = promptEvalCount;
    }

    @JsonProperty("eval_count")
    public Integer getEvalCount() {
        return evalCount;
    }

    @JsonProperty("eval_count")
    public void setEvalCount(Integer evalCount) {
        this.evalCount = evalCount;
    }
}
