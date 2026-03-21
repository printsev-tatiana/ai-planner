# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build
COPY aiplanner/ .
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/aiplanner-0.0.1-SNAPSHOT.jar app.jar

# Expose port (adjust if your app runs on a different port)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.launch.JarLauncher || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
