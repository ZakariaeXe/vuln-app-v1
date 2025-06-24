# Use a Java base image
#FROM openjdk:21-jdk-slim as builder

#Build image with maven
FROM maven:3.9.4-eclipse-temurin-21 as builder
# Set working directory
WORKDIR /app
# Copy Maven pom.xml and src/ to separate layers for caching
COPY pom.xml .
COPY src ./src
# Build the application (using Maven Wrapper if present, or just 'mvn')
# -DskipTests to skip tests here (tests run in earlier CI step)
RUN mvn clean package -Dmaven.test.skip=true

# --- Runtime image ---
FROM eclipse-temurin:21-jre
# Set working directory
WORKDIR /app
# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar
# Expose the port your Spring Boot app runs on (e.g., 8080)
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
