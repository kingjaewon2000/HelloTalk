FROM openjdk:21-jdk-slim

WORKDIR /app

COPY chat-server.jar /app/chat-server.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms8g -Xmx12g"

ENTRYPOINT ["java", "-jar", "/app/chat-server.jar"]