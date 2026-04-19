package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    private ErrorBody error;

    public ErrorResponse() {
    }

    public ErrorResponse(ErrorBody error) {
        this.error = error;
    }

    @JsonProperty
    public ErrorBody getError() {
        return error;
    }

    @JsonProperty
    public void setError(ErrorBody error) {
        this.error = error;
    }
}
