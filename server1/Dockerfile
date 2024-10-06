
FROM gradle:7.6-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle build --no-daemon

FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y procps

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/service1.jar

EXPOSE 8199

ENTRYPOINT ["java", "-jar", "/app/service1.jar"]
