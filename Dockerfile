FROM ubuntu:latest as build

RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-21-jdk maven

WORKDIR /app

COPY . .

RUN mvn clean install

FROM openjdk:21-jdk-slim

EXPOSE 8080

COPY --from=build /app/target/planner-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
