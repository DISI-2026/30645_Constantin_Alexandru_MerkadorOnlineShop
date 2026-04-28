# Use a base image with Java 21
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files to enable building inside the container
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml and download dependencies to leverage Docker cache
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# Copy the rest of the application source code
COPY src src

# Build the application
RUN ./mvnw clean install -DskipTests

# Expose the port the application runs on
EXPOSE 8086

# Command to run the application
ENTRYPOINT ["java", "-jar", "target/post-service-0.0.1-SNAPSHOT.jar"]
