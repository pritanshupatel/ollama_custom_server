package com.example.ollamaadapter.core;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.util.UUID;

@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        String endpoint = requestContext.getMethod() + " " + requestContext.getUriInfo().getPath();
        RequestContext.initialize(requestId, endpoint);
        MDC.put("requestId", requestId);
        requestContext.setProperty(REQUEST_ID_HEADER, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String requestId = (String) requestContext.getProperty(REQUEST_ID_HEADER);
        responseContext.getHeaders().putSingle(REQUEST_ID_HEADER, requestId);
    }
}
