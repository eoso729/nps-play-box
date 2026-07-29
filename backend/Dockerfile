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

EXPOSE 8023

ENTRYPOINT ["java", "-jar", "app.jar"]
