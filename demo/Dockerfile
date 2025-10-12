# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set working directory inside the container
WORKDIR /build

# Copy only the files needed for Maven build
COPY pom.xml .
COPY src ./src

# Build the project without tests
RUN mvn clean package -DskipTests

# -------- Run stage --------
FROM eclipse-temurin:21-jdk-jammy

# Working directory for runtime
WORKDIR /app

# Copy the jar from build stage (adjust name if your jar is different)
COPY --from=build /build/target/*.jar app.jar

# Use Render's $PORT environment variable
ENV PORT 8080

# Expose the port (optional but good practice)
EXPOSE 8080

# Run the app with dynamic port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]
