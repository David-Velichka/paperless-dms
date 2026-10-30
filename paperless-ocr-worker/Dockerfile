# Multi-Stage Build for the OCR Worker (Java 25)

# Stage 1: Build & Dependencies Cache
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

# Copy gradle wrapper and build configs first for caching dependencies
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle/ ./gradle/
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# Copy source code and build jar
COPY src/ ./src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

# Run as non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
