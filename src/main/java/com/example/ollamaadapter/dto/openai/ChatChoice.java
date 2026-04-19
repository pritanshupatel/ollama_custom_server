package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatChoice {

    private int index;
    private ChatMessage message;
    private String finishReason;

    public ChatChoice() {
    }

    public ChatChoice(int index, ChatMessage message, String finishReason) {
        this.index = index;
        this.message = message;
        this.finishReason = finishReason;
    }

    @JsonProperty
    public int getIndex() {
        return index;
    }

    @JsonProperty
    public void setIndex(int index) {
        this.index = index;
    }

    @JsonProperty
    public ChatMessage getMessage() {
        return message;
    }

    @JsonProperty
    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    @JsonProperty("finish_reason")
    public String getFinishReason() {
        return finishReason;
    }

    @JsonProperty("finish_reason")
    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}
