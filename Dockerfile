# Use a Java base image
FROM openjdk:24-jdk-slim as builder
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Define Maven version (you can customize)
ARG MAVEN_VERSION=3.8.8
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

# Download and install Maven
RUN curl -fsSL ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.zip -o /tmp/apache-maven.zip \
    && unzip /tmp/apache-maven.zip -d /opt/ \
    && rm /tmp/apache-maven.zip \
    && mv /opt/apache-maven-${MAVEN_VERSION} /opt/maven

# Set Maven environment variables
ENV M2_HOME /opt/maven
ENV PATH ${M2_HOME}/bin:${PATH}

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
