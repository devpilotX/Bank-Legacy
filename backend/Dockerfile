# Build the jar with the Maven wrapper, then run it on a small JRE.
# The .dockerignore keeps application-local.properties (which holds local secrets)
# out of the image, so nothing secret is ever baked in.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
# Run as a non-root user, and add curl just for the health check.
RUN groupadd -r app && useradd -r -g app -u 1001 app \
 && apt-get update && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /data/storage && chown -R app:app /data /app
USER app
ENV APP_STORAGE_ROOT=/data/storage
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
