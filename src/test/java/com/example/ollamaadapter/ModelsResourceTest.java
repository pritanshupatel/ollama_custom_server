package com.example.ollamaadapter;

import com.example.ollamaadapter.api.ModelsResource;
import com.example.ollamaadapter.client.OllamaClient;
import com.example.ollamaadapter.core.RequestIdFilter;
import com.example.ollamaadapter.dto.ollama.OllamaModelInfo;
import com.example.ollamaadapter.dto.ollama.OllamaTagsResponse;
import com.example.ollamaadapter.exception.GlobalExceptionMapper;
import com.example.ollamaadapter.service.ModelListingService;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
class ModelsResourceTest {

    private static final OllamaClient OLLAMA_CLIENT = Mockito.mock(OllamaClient.class);

    private static final ResourceExtension RESOURCE = ResourceExtension.builder()
        .addProvider(new RequestIdFilter())
        .addProvider(new GlobalExceptionMapper())
        .addResource(new ModelsResource(new ModelListingService(OLLAMA_CLIENT, "qwen3.5:9b")))
        .build();

    @Test
    void modelsReturnsMappedOllamaModels() {
        when(OLLAMA_CLIENT.listModels()).thenReturn(
            new OllamaTagsResponse(List.of(new OllamaModelInfo("qwen3.5:9b")))
        );

        String body = RESOURCE.target("/v1/models").request().get(String.class);

        assertTrue(body.contains("\"object\":\"list\""));
        assertTrue(body.contains("\"id\":\"qwen3.5:9b\""));
        assertTrue(body.contains("\"owned_by\":\"ollama\""));
    }
}
