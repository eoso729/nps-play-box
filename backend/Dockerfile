# Multi-stage Docker build for Spring Boot Backend
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy dependencies manifest and source
COPY pom.xml .
COPY src ./src

# Package application
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy compiled executable JAR
COPY --from=build /app/target/*.jar app.jar

# Copy key files into the runtime image at a stable absolute path
COPY --from=build /app/src/main/java/org/example/signer/keys /app/keys

EXPOSE 8023

ENTRYPOINT ["java", "-jar", "app.jar"]
