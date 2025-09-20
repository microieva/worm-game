FROM maven:3.8.6-openjdk-11 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim

LABEL maintainer="ievavyl@yahoo.com"

WORKDIR /app

COPY --from=builder /app/target/wormgame-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

RUN groupadd -r appgroup && useradd -r -g appgroup appuser && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
