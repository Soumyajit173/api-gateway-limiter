# -------------------------
# API Gateway Dockerfile
# -------------------------

# Use official OpenJDK image
FROM eclipse-temurin:21-jdk AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and project files
COPY . .

# Build the application (skip tests for faster CI/CD)
RUN ./mvnw clean package -DskipTests

# -------------------------
# Runtime image
# -------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the fat JAR from build stage
COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Render will override with $PORT)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
