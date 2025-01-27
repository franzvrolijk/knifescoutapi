FROM eclipse-temurin:21

WORKDIR /app

COPY build/libs/api-all.jar /app

EXPOSE 8080

CMD ["java", "-jar", "/app/api-all.jar"]