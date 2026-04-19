package com.example.ollamaadapter.exception;

public class ApiException extends RuntimeException {

    private final int status;
    private final String code;
    private final String type;
    private final String param;

    public ApiException(int status, String message, String code) {
        this(status, message, "adapter_error", null, code, null);
    }

    public ApiException(int status, String message, String code, Throwable cause) {
        this(status, message, "adapter_error", null, code, cause);
    }

    public ApiException(int status, String message, String type, String param, String code, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
        this.type = type;
        this.param = param;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getParam() {
        return param;
    }
}
