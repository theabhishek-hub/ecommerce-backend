# ============================================================================
# Multi-stage Dockerfile for AbhiOnlineDukaan - Optimized for Render
# Stage 1: Build the application
# Stage 2: Run the application with minimal image size
# ============================================================================

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy only pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# ============================================================================
# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Expose port (Render will set PORT env variable)
EXPOSE ${PORT:-8080}

# Run the application
# Set reasonable JVM options for containerized environment
ENTRYPOINT ["sh", "-c", "java -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -Dserver.port=${PORT:-8080} -jar app.jar"]
