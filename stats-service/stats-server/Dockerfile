FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar stats-app.jar
ENTRYPOINT ["java","-jar","/stats-app.jar"]