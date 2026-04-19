package com.example.ollamaadapter;

import com.example.ollamaadapter.dto.ollama.OllamaChatRequest;
import com.example.ollamaadapter.dto.ollama.OllamaChatResponse;
import com.example.ollamaadapter.dto.ollama.OllamaMessage;
import com.example.ollamaadapter.dto.openai.ChatCompletionRequest;
import com.example.ollamaadapter.dto.openai.ChatCompletionResponse;
import com.example.ollamaadapter.dto.openai.ChatMessage;
import com.example.ollamaadapter.dto.openai.OpenAiContentPart;
import com.example.ollamaadapter.exception.ApiException;
import com.example.ollamaadapter.service.OpenAiMessageNormalizer;
import com.example.ollamaadapter.service.ResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResponseMapperTest {

    private final ResponseMapper responseMapper = new ResponseMapper("qwen3.5:9b", new OpenAiMessageNormalizer(new ObjectMapper()));

    @Test
    void mapsOpenAiRequestToOllamaRequest() {
        ChatCompletionRequest request = new ChatCompletionRequest(
            "qwen3.5:9b",
            List.of(new ChatMessage("user", "Hello")),
            0.2,
            0.9,
            200,
            false
        );

        OllamaChatRequest mapped = responseMapper.toOllamaRequest(request, "qwen3.5:9b");

        assertEquals("qwen3.5:9b", mapped.getModel());
        assertFalse(mapped.isStream());
        assertEquals(1, mapped.getMessages().size());
        assertEquals(200, mapped.getOptions().get("num_predict"));
    }

    @Test
    void mapsContentPartsToPlainTextForOllama() {
        ChatCompletionRequest request = new ChatCompletionRequest(
            "qwen3.5:9b",
            List.of(
                new ChatMessage(
                    "user",
                    List.of(
                        new OpenAiContentPart("text", "Hello"),
                        new OpenAiContentPart("image_url", null),
                        new OpenAiContentPart("text", "World")
                    )
                )
            ),
            null,
            null,
            null,
            false
        );

        OllamaChatRequest mapped = responseMapper.toOllamaRequest(request, "qwen3.5:9b");

        assertEquals("Hello\nWorld", mapped.getMessages().get(0).getContent());
    }

    @Test
    void mapsOllamaResponseToOpenAiResponse() {
        OllamaChatResponse ollamaResponse = new OllamaChatResponse(
            "qwen3.5:9b",
            new OllamaMessage("assistant", "Hello back"),
            "stop",
            12,
            4
        );

        ChatCompletionResponse response = responseMapper.toOpenAiResponse(ollamaResponse, "qwen3.5:9b");

        assertEquals("chat.completion", response.getObject());
        assertEquals(1, response.getChoices().size());
        assertEquals("Hello back", response.getChoices().get(0).getMessage().getContent());
        assertEquals(16, response.getUsage().getTotalTokens());
    }

    @Test
    void malformedOllamaResponseThrowsNormalizedError() {
        ApiException exception = assertThrows(
            ApiException.class,
            () -> responseMapper.toOpenAiResponse(new OllamaChatResponse(), "qwen3.5:9b")
        );

        assertEquals("invalid_upstream_response", exception.getCode());
    }
}
