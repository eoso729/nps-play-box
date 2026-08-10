FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy locally compiled executable JAR
COPY target/*.jar app.jar

# Copy key files into the runtime image
COPY src/main/java/org/example/signer/keys /app/keys

EXPOSE 8023

ENTRYPOINT ["java", "-jar", "app.jar"]
