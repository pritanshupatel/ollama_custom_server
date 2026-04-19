package com.example.ollamaadapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.server.ServerFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class AdapterConfiguration extends Configuration {

    @NotBlank
    private String host = "0.0.0.0";

    @Min(1)
    @Max(65535)
    private int port = 8080;

    @NotBlank
    private String ollamaBaseUrl = "http://localhost:11434";

    @NotBlank
    private String defaultModel = "qwen3.5:9b";

    private String apiKey = "";

    @Min(1)
    @Max(600)
    private int requestTimeoutSeconds = 60;

    @Min(1)
    @Max(10_000)
    private int maxConcurrentRequests = 16;

    @NotBlank
    private String logLevel = "INFO";

    private boolean debugRequestResponseLogging = true;

    private boolean debugLogRawAuthHeaders = false;

    @Min(1)
    @Max(1_000_000)
    private int maxLoggedBodyChars = 20_000;

    @JsonIgnore
    private ServerFactory serverFactory;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }

    @JsonProperty
    public void setOllamaBaseUrl(String ollamaBaseUrl) {
        this.ollamaBaseUrl = ollamaBaseUrl;
    }

    @JsonProperty
    public String getDefaultModel() {
        return defaultModel;
    }

    @JsonProperty
    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    @JsonProperty
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    @JsonProperty
    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    @JsonProperty
    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    @JsonProperty
    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    @JsonProperty
    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    @JsonProperty
    public String getLogLevel() {
        return logLevel;
    }

    @JsonProperty
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    @JsonProperty
    public boolean isDebugRequestResponseLogging() {
        return debugRequestResponseLogging;
    }

    @JsonProperty
    public void setDebugRequestResponseLogging(boolean debugRequestResponseLogging) {
        this.debugRequestResponseLogging = debugRequestResponseLogging;
    }

    @JsonProperty
    public boolean isDebugLogRawAuthHeaders() {
        return debugLogRawAuthHeaders;
    }

    @JsonProperty
    public void setDebugLogRawAuthHeaders(boolean debugLogRawAuthHeaders) {
        this.debugLogRawAuthHeaders = debugLogRawAuthHeaders;
    }

    @JsonProperty
    public int getMaxLoggedBodyChars() {
        return maxLoggedBodyChars;
    }

    @JsonProperty
    public void setMaxLoggedBodyChars(int maxLoggedBodyChars) {
        this.maxLoggedBodyChars = maxLoggedBodyChars;
    }

    @Override
    @JsonProperty("server")
    public ServerFactory getServerFactory() {
        if (serverFactory == null) {
            DefaultServerFactory factory = new DefaultServerFactory();

            HttpConnectorFactory applicationConnector = new HttpConnectorFactory();
            applicationConnector.setBindHost(host);
            applicationConnector.setPort(port);

            HttpConnectorFactory adminConnector = new HttpConnectorFactory();
            adminConnector.setBindHost(host);
            adminConnector.setPort(port + 1);

            factory.setApplicationConnectors(java.util.List.of(applicationConnector));
            factory.setAdminConnectors(java.util.List.of(adminConnector));
            serverFactory = factory;
        }
        return serverFactory;
    }

    @JsonProperty("server")
    public void setServerFactory(ServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }
}
