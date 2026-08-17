FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

ARG JAR_FILE
COPY ${JAR_FILE} /app/application.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/application.jar"]
