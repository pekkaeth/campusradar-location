FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

FROM tomcat:9.0-jdk17
RUN rm -rf /usr/local/tomcat/webapps/*
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml
COPY --from=build /app/target/campusradar.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]