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

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port (Render will set PORT env variable)

# Run the application
# Set reasonable JVM options for containerized environment
ENTRYPOINT ["sh", "-c", "java -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -Dserver.port=${PORT} -Dspring.profiles.active=prod -jar app.jar"]
