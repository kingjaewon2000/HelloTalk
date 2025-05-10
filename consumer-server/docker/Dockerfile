FROM openjdk:21-jdk-slim

WORKDIR /app

COPY consumer-server.jar /app/consumer-server.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/consumer-server.jar"]