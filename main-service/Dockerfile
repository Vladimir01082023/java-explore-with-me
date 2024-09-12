FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar main-app.jar
ENTRYPOINT ["java","-jar","/main-app.jar"]