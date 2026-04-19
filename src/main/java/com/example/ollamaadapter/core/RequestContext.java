package com.example.ollamaadapter.core;

public final class RequestContext {

    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    private RequestContext() {
    }

    public static void initialize(String requestId, String endpoint) {
        State state = new State();
        state.setRequestId(requestId);
        state.setEndpoint(endpoint);
        state.setStartTimeMillis(System.currentTimeMillis());
        STATE.set(state);
    }

    public static void clear() {
        STATE.remove();
    }

    public static String getRequestId() {
        return STATE.get().getRequestId();
    }

    public static void setModel(String model) {
        STATE.get().setModel(model);
    }

    public static String getModel() {
        return STATE.get().getModel();
    }

    public static void setErrorSummary(String errorSummary) {
        STATE.get().setErrorSummary(errorSummary);
    }

    public static String getErrorSummary() {
        return STATE.get().getErrorSummary();
    }

    public static void setFailureCategory(String failureCategory) {
        STATE.get().setFailureCategory(failureCategory);
    }

    public static String getFailureCategory() {
        return STATE.get().getFailureCategory();
    }

    public static String getEndpoint() {
        return STATE.get().getEndpoint();
    }

    public static long getStartTimeMillis() {
        return STATE.get().getStartTimeMillis();
    }

    public static void setRequestBody(String requestBody) {
        STATE.get().setRequestBody(requestBody);
    }

    public static String getRequestBody() {
        return STATE.get().getRequestBody();
    }

    public static void setRequestHeaders(String requestHeaders) {
        STATE.get().setRequestHeaders(requestHeaders);
    }

    public static String getRequestHeaders() {
        return STATE.get().getRequestHeaders();
    }

    public static void setResponseHeaders(String responseHeaders) {
        STATE.get().setResponseHeaders(responseHeaders);
    }

    public static String getResponseHeaders() {
        return STATE.get().getResponseHeaders();
    }

    public static void setResponseBody(String responseBody) {
        STATE.get().setResponseBody(responseBody);
    }

    public static String getResponseBody() {
        return STATE.get().getResponseBody();
    }

    public static void setResponseStatus(int responseStatus) {
        STATE.get().setResponseStatus(responseStatus);
    }

    public static int getResponseStatus() {
        return STATE.get().getResponseStatus();
    }

    public static void setResponseLogged(boolean responseLogged) {
        STATE.get().setResponseLogged(responseLogged);
    }

    public static boolean isResponseLogged() {
        return STATE.get().isResponseLogged();
    }

    public static void setRequestPath(String requestPath) {
        STATE.get().setRequestPath(requestPath);
    }

    public static String getRequestPath() {
        return STATE.get().getRequestPath();
    }

    private static final class State {
        private String requestId;
        private String endpoint;
        private String model;
        private String errorSummary;
        private String failureCategory;
        private String requestBody;
        private String requestHeaders;
        private String requestPath;
        private String responseHeaders;
        private String responseBody;
        private int responseStatus;
        private boolean responseLogged;
        private long startTimeMillis;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getErrorSummary() {
            return errorSummary;
        }

        public void setErrorSummary(String errorSummary) {
            this.errorSummary = errorSummary;
        }

        public String getFailureCategory() {
            return failureCategory;
        }

        public void setFailureCategory(String failureCategory) {
            this.failureCategory = failureCategory;
        }

        public String getRequestBody() {
            return requestBody;
        }

        public void setRequestBody(String requestBody) {
            this.requestBody = requestBody;
        }

        public String getRequestHeaders() {
            return requestHeaders;
        }

        public void setRequestHeaders(String requestHeaders) {
            this.requestHeaders = requestHeaders;
        }

        public String getRequestPath() {
            return requestPath;
        }

        public void setRequestPath(String requestPath) {
            this.requestPath = requestPath;
        }

        public String getResponseHeaders() {
            return responseHeaders;
        }

        public void setResponseHeaders(String responseHeaders) {
            this.responseHeaders = responseHeaders;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        public int getResponseStatus() {
            return responseStatus;
        }

        public void setResponseStatus(int responseStatus) {
            this.responseStatus = responseStatus;
        }

        public boolean isResponseLogged() {
            return responseLogged;
        }

        public void setResponseLogged(boolean responseLogged) {
            this.responseLogged = responseLogged;
        }

        public long getStartTimeMillis() {
            return startTimeMillis;
        }

        public void setStartTimeMillis(long startTimeMillis) {
            this.startTimeMillis = startTimeMillis;
        }
    }
}
