package com.example.ollamaadapter.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorBody {

    private String message;
    private String type;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String param;

    private String code;

    public ErrorBody() {
    }

    public ErrorBody(String message, String type, String param, String code) {
        this.message = message;
        this.type = type;
        this.param = param;
        this.code = code;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @JsonProperty
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty
    public String getType() {
        return type;
    }

    @JsonProperty
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty
    public String getParam() {
        return param;
    }

    @JsonProperty
    public void setParam(String param) {
        this.param = param;
    }

    @JsonProperty
    public String getCode() {
        return code;
    }

    @JsonProperty
    public void setCode(String code) {
        this.code = code;
    }
}
