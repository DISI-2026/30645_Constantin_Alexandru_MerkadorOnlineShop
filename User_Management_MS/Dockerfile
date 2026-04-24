# Use a small OpenJDK runtime image
FROM eclipse-temurin:25-jdk-alpine

# Create a directory for the app
WORKDIR /app

# Copy the built jar into the container
COPY target/*.jar app.jar

# Expose the same port that Spring Boot uses inside the container
EXPOSE 8082

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]
