# ollama-openai-adapter

## Overview
This project is a production-style Java 17 adapter server built with Dropwizard and Jersey. It exposes OpenAI-compatible endpoints for chat completions and model listing, while forwarding requests to a local Ollama server.

It is intended for clients such as Unsloth Studio that expect `/v1` OpenAI-style APIs but need to run against locally hosted Ollama models.

## Features
- Java 17, Maven, Dropwizard 4.x, Jersey, Jackson
- `GET /health`
- `GET /v1/models`
- `POST /v1/chat/completions`
- Optional bearer-token authentication
- OpenAI-style error responses
- Semaphore-based concurrency limiting
- Outbound timeout handling with Java `HttpClient`
- Structured request logging with request IDs
- JUnit 5 unit tests

## Prerequisites
- Java 17+
- Maven 3.9+
- Ollama installed locally

## Run Ollama
Install and start Ollama from the official project, then verify it is reachable:

```bash
ollama serve
```

In a separate shell, pull a model:

```bash
ollama pull qwen3.5:9b
```

## Configuration
Main settings live in `config.yml` and support environment substitution.

Important values:
- `host`
- `port`
- `ollamaBaseUrl`
- `defaultModel`
- `apiKey`
- `requestTimeoutSeconds`
- `maxConcurrentRequests`
- `logLevel`

If `apiKey` is blank, auth is disabled. If it is set, clients must send:

```http
Authorization: Bearer <API_KEY>
```

## Build
```bash
mvn clean package
```

## Run locally
```bash
java -jar target/ollama-openai-adapter-1.0.0.jar server config.yml
```

The application port defaults to `8080`. Dropwizard admin endpoints use `8081`.

## Endpoint examples

### Health
```bash
curl http://localhost:8080/health
```

### List models
If auth is disabled:

```bash
curl http://localhost:8080/v1/models
```

If auth is enabled:

```bash
curl -H "Authorization: Bearer your-secret" http://localhost:8080/v1/models
```

### Chat completions
If auth is disabled:

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen3.5:9b",
    "messages": [
      {"role": "system", "content": "You are helpful."},
      {"role": "user", "content": "Hello"}
    ],
    "temperature": 0.1,
    "top_p": 1.0,
    "max_tokens": 300,
    "stream": false
  }'
```

If auth is enabled:

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Authorization: Bearer your-secret" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen3.5:9b",
    "messages": [
      {"role": "user", "content": "Explain semaphores briefly."}
    ],
    "stream": false
  }'
```

## Unsloth Studio configuration
Point Unsloth to:
- endpoint: `http://localhost:8080/v1`
- api key: configured `API_KEY`, or any dummy string if auth is disabled but the UI requires a value
- model id: `qwen3.5:9b`

## Notes on Ollama mapping
- OpenAI `messages` are mapped directly to Ollama chat messages.
- `max_tokens` is approximated to Ollama `num_predict`.
- `temperature` and `top_p` are forwarded through `options`.
- Non-streaming mode is implemented.
- If `stream=true` is requested, the adapter returns `400` with OpenAI-style error JSON.
- If Ollama does not provide token usage counts, usage values default to `0`.

## Logging
- Persistent file logging is enabled through Dropwizard and writes to `logs/adapter.log`.
- Rolled daily archives are written as `logs/adapter-%d.log.gz`, for example `logs/adapter-2026-04-17.log.gz`.
- The `logs/` directory is created automatically during startup.
- Dropwizard writes the active log to `logs/adapter.log` and rolls archives daily using the configured filename pattern.
- Console logging remains enabled for local development and operations visibility.
- Each log line includes timestamp, level, `requestId`, thread name, logger/class name, and message.
- `requestId` is propagated through MDC so all request-scoped logs can be traced end-to-end.
- Incoming API requests, outgoing API responses, outgoing Ollama calls, Ollama responses, validation failures, timeouts, and unhandled exceptions are logged.
- Secrets such as API keys and authorization header values are not logged.
- Full stack traces are written for handled REST exceptions and unexpected internal exceptions.

## Windows notes
The project works on Windows local development with PowerShell and standard Maven commands.

## Test
```bash
mvn test
```
