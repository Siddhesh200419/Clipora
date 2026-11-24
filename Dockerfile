FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package

FROM tomcat:10.1-jdk21-temurin
COPY --from=build /app/target/clipora.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
git add src/main/java/com/clipora/utils/DatabaseConnection.java && git commit -m "Add database connection utility"