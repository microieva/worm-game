
# Stage 1: Build the application
FROM maven:3.8.6-openjdk-11 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Create runtime image
FROM openjdk:11-jre-slim

LABEL maintainer="ievavyl@yahoo.com"

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/snake-game-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

RUN groupadd -r snakegroup && useradd -r -g snakegroup snakeuser && \
    chown -R snakeuser:snakegroup /app

USER snakeuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "com.portfolio.wormgame.Main"]

HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/health || exit 1