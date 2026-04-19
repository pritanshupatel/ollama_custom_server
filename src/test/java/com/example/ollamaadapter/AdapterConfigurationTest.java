package com.example.ollamaadapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterConfigurationTest {

    @Test
    void apiKeyDefaultsToEmptyStringWhenOmitted() {
        AdapterConfiguration configuration = new AdapterConfiguration();

        assertEquals("", configuration.getApiKey());
        assertFalse(AdapterApplication.isApiKeyEnabled(configuration.getApiKey()));
    }

    @Test
    void nullApiKeyIsNormalizedToEmptyString() {
        AdapterConfiguration configuration = new AdapterConfiguration();

        configuration.setApiKey(null);

        assertEquals("", configuration.getApiKey());
        assertFalse(AdapterApplication.isApiKeyEnabled(configuration.getApiKey()));
    }

    @Test
    void emptyApiKeyDisablesAuth() {
        AdapterConfiguration configuration = new AdapterConfiguration();

        configuration.setApiKey("");

        assertEquals("", configuration.getApiKey());
        assertFalse(AdapterApplication.isApiKeyEnabled(configuration.getApiKey()));
    }

    @Test
    void blankApiKeyDisablesAuth() {
        AdapterConfiguration configuration = new AdapterConfiguration();

        configuration.setApiKey("   ");

        assertEquals("   ", configuration.getApiKey());
        assertFalse(AdapterApplication.isApiKeyEnabled(configuration.getApiKey()));
    }

    @Test
    void nonBlankApiKeyEnablesAuth() {
        AdapterConfiguration configuration = new AdapterConfiguration();

        configuration.setApiKey("secret-key");

        assertEquals("secret-key", configuration.getApiKey());
        assertTrue(AdapterApplication.isApiKeyEnabled(configuration.getApiKey()));
    }
}
