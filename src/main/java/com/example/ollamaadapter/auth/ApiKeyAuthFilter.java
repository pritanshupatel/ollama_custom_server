package com.example.ollamaadapter.auth;

import com.example.ollamaadapter.core.RequestContext;
import com.example.ollamaadapter.exception.ApiException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

public class ApiKeyAuthFilter implements ContainerRequestFilter {

    private final String configuredApiKey;

    public ApiKeyAuthFilter(String configuredApiKey) {
        this.configuredApiKey = configuredApiKey == null ? "" : configuredApiKey.trim();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (configuredApiKey.isEmpty()) {
            return;
        }

        String authorization = requestContext.getHeaderString("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            RequestContext.setErrorSummary("missing_or_invalid_authorization_header");
            throw new ApiException(401, "Missing bearer token.", "unauthorized");
        }

        String providedToken = authorization.substring("Bearer ".length()).trim();
        if (!configuredApiKey.equals(providedToken)) {
            RequestContext.setErrorSummary("invalid_api_key");
            throw new ApiException(401, "Invalid API key.", "unauthorized");
        }
    }
}
