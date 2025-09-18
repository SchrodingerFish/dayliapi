FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/dailyapi.jar dailyapi.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "dailyapi.jar"]