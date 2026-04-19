package com.example.ollamaadapter;

import com.example.ollamaadapter.api.HealthResource;
import com.example.ollamaadapter.core.RequestIdFilter;
import com.example.ollamaadapter.exception.GlobalExceptionMapper;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DropwizardExtensionsSupport.class)
class HealthResourceTest {

    private static final ResourceExtension RESOURCE = ResourceExtension.builder()
        .addProvider(new RequestIdFilter())
        .addProvider(new GlobalExceptionMapper())
        .addResource(new HealthResource())
        .build();

    @Test
    void healthReturnsOkPayload() {
        String body = RESOURCE.target("/health").request().get(String.class);

        assertTrue(body.contains("\"status\":\"ok\""));
        assertTrue(body.contains("\"service\":\"ollama-openai-adapter\""));
    }
}
