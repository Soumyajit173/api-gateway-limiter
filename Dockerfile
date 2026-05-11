# -------------------------
# API Gateway Dockerfile
# -------------------------

# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy project files
COPY . .

# Ensure Maven wrapper is executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# -------------------------
# Runtime stage
# -------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
