FROM maven:3.8-openjdk-11 AS build
COPY pom.xml /app/pom.xml
WORKDIR /app

# fetch all dependencies
RUN mvn dependency:go-offline -B

COPY src /app/src

RUN mvn package war:war

FROM jetty:10-jre11 AS server
COPY --from=build /app/target/*.war /var/lib/jetty/webapps/ROOT.war
