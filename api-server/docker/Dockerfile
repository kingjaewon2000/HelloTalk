FROM openjdk:21-jdk-slim

WORKDIR /app

COPY api-server.jar /app/api-server.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/api-server.jar"]