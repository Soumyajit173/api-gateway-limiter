# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy only the maven wrapper and pom first to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Download dependencies without copying full source yet
RUN ./mvnw dependency:go-offline

# Now copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root user for better security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

# Standard Spring Boot port
EXPOSE 8080

# Use the 'prod' profile by default if you decide to use one later
# ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]