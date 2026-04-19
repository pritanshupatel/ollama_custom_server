FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/ollama-openai-adapter-1.0.0.jar /app/adapter.jar
COPY config.yml /app/config.yml

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "/app/adapter.jar", "server", "/app/config.yml"]
