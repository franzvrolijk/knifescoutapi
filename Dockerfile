FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/api-all.jar .

EXPOSE 8080

CMD ["java", "-jar", "/app/api-all.jar"]