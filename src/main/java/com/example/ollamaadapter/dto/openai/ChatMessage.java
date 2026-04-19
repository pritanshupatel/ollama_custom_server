package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    @NotBlank(message = "message role must not be blank")
    private String role;

    @NotNull(message = "message content must not be null")
    private Object content;

    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String role, List<OpenAiContentPart> content) {
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
    public Object getContent() {
        return content;
    }

    @JsonProperty
    public void setContent(Object content) {
        this.content = content;
    }

    public boolean hasStringContent() {
        return content instanceof String;
    }

    public boolean hasArrayContent() {
        return content instanceof List<?>;
    }

    public String getContentKind() {
        if (hasStringContent()) {
            return "string";
        }
        if (hasArrayContent()) {
            return "array";
        }
        if (content == null) {
            return "null";
        }
        return content.getClass().getSimpleName();
    }
}
