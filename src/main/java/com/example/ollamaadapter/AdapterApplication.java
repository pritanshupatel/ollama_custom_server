package com.example.ollamaadapter;

import com.example.ollamaadapter.api.ChatCompletionsResource;
import com.example.ollamaadapter.api.HealthResource;
import com.example.ollamaadapter.api.ModelsResource;
import com.example.ollamaadapter.auth.ApiKeyAuthFilter;
import com.example.ollamaadapter.client.OllamaClient;
import com.example.ollamaadapter.client.OllamaClientImpl;
import com.example.ollamaadapter.core.ApiLoggingFilter;
import com.example.ollamaadapter.core.ConcurrencyGuard;
import com.example.ollamaadapter.core.RequestIdFilter;
import com.example.ollamaadapter.exception.GlobalExceptionMapper;
import com.example.ollamaadapter.health.AdapterHealthCheck;
import com.example.ollamaadapter.service.ChatCompletionService;
import com.example.ollamaadapter.service.ModelListingService;
import com.example.ollamaadapter.service.OpenAiMessageNormalizer;
import com.example.ollamaadapter.service.ResponseMapper;
import io.dropwizard.core.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.http.HttpClient;
import java.time.Duration;

public class AdapterApplication extends Application<AdapterConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterApplication.class);

    public static void main(String[] args) throws Exception {
        createLogsDirectory();
        new AdapterApplication().run(args);
    }

    @Override
    public String getName() {
        return "ollama-openai-adapter";
    }

    @Override
    public void initialize(Bootstrap<AdapterConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            )
        );
    }

    @Override
    public void run(AdapterConfiguration configuration, Environment environment) {
        String apiKey = configuration.getApiKey();
        boolean apiKeyEnabled = isApiKeyEnabled(apiKey);

        LOGGER.info("application startup initiated");
        LOGGER.info(
            "config loaded host={} port={} ollamaBaseUrl={} defaultModel={} requestTimeoutSeconds={} maxConcurrentRequests={} authEnabled={} debugRequestResponseLogging={} debugLogRawAuthHeaders={} maxLoggedBodyChars={}",
            configuration.getHost(),
            configuration.getPort(),
            configuration.getOllamaBaseUrl(),
            configuration.getDefaultModel(),
            configuration.getRequestTimeoutSeconds(),
            configuration.getMaxConcurrentRequests(),
            apiKeyEnabled,
            configuration.isDebugRequestResponseLogging(),
            configuration.isDebugLogRawAuthHeaders(),
            configuration.getMaxLoggedBodyChars()
        );
        if (apiKeyEnabled) {
            LOGGER.info("API key auth is enabled");
        } else {
            LOGGER.info("API key auth is disabled");
        }

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(configuration.getRequestTimeoutSeconds()))
            .build();

        OllamaClient ollamaClient = new OllamaClientImpl(
            httpClient,
            environment.getObjectMapper(),
            configuration.getOllamaBaseUrl(),
            Duration.ofSeconds(configuration.getRequestTimeoutSeconds())
        );

        OpenAiMessageNormalizer openAiMessageNormalizer = new OpenAiMessageNormalizer(environment.getObjectMapper());
        ResponseMapper responseMapper = new ResponseMapper(configuration.getDefaultModel(), openAiMessageNormalizer);
        ConcurrencyGuard concurrencyGuard = new ConcurrencyGuard(configuration.getMaxConcurrentRequests());
        ChatCompletionService chatCompletionService = new ChatCompletionService(
            ollamaClient,
            responseMapper,
            concurrencyGuard,
            configuration.getDefaultModel()
        );
        ModelListingService modelListingService = new ModelListingService(
            ollamaClient,
            configuration.getDefaultModel()
        );

        environment.jersey().register(new RequestIdFilter());
        environment.jersey().register(
            new ApiLoggingFilter(
                configuration.isDebugRequestResponseLogging(),
                configuration.isDebugLogRawAuthHeaders(),
                configuration.getMaxLoggedBodyChars(),
                environment.getObjectMapper()
            )
        );
        if (apiKeyEnabled) {
            environment.jersey().register(new ApiKeyAuthFilter(apiKey));
        }
        environment.jersey().register(new GlobalExceptionMapper());

        environment.jersey().register(new HealthResource());
        environment.jersey().register(new ModelsResource(modelListingService));
        environment.jersey().register(new ChatCompletionsResource(chatCompletionService));

        environment.healthChecks().register("ollama", new AdapterHealthCheck(ollamaClient));
        environment.lifecycle().addServerLifecycleListener(new StartupLifecycleLogger());
        environment.lifecycle().manage(new ShutdownLifecycleLogger());
    }

    static boolean isApiKeyEnabled(String apiKey) {
        return apiKey != null && !apiKey.isBlank();
    }

    private static void createLogsDirectory() throws IOException {
        Files.createDirectories(Path.of("logs"));
    }

    private static class StartupLifecycleLogger implements ServerLifecycleListener {

        @Override
        public void serverStarted(Server server) {
            LOGGER.info("server started connectors={}", server.getConnectors().length);
        }
    }

    private static class ShutdownLifecycleLogger implements Managed {

        @Override
        public void start() {
            LOGGER.info("application startup completed");
        }

        @Override
        public void stop() {
            LOGGER.info("shutdown initiated");
            LOGGER.info("shutdown completed");
        }
    }
}
