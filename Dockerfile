FROM maven:3.8-openjdk-11 AS build
COPY pom.xml /app/pom.xml
WORKDIR /app

# fetch all dependencies
RUN mvn dependency:go-offline -B

COPY src /app/src

RUN mvn package

FROM openjdk:11-jdk
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Xmx512m", "/app.jar"]
