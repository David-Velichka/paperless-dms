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
FROM eclipse-temurin:25-jre-resolute AS runtime
WORKDIR /app

# Tesseract (OCR) and Ghostscript (PDF to image)
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng tesseract-ocr-deu ghostscript \
    && rm -rf /var/lib/apt/lists/*
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

# Run as non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

# Tess4J loads libtesseract through JNA, this flag avoids the native access warning of Java 25
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
