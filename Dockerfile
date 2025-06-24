# Use a Java base image
FROM openjdk:21-jdk-slim as builder

FROM maven:3.9.4-eclipse-temurin-21

# Set working directory
WORKDIR /app

# Copy Maven pom.xml and src/ to separate layers for caching
COPY pom.xml .
COPY src ./src

# Build the application (using Maven Wrapper if present, or just 'mvn')
# -DskipTests to skip tests here (tests run in earlier CI step)
RUN mvn clean package -Dmaven.test.skip=true

# --- Runtime image ---
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar demo-0.0.1-SNAPSHOT.jar

# Expose the port your Spring Boot app runs on (e.g., 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]
