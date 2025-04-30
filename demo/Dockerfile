FROM openjdk:17
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} Karting-Back-end.jar
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "./Karting-Back-end.jar"]