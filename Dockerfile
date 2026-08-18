FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S gym && adduser -S gym -G gym
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

USER gym
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
