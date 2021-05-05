FROM 090922436798.dkr.ecr.us-east-1.amazonaws.com/jdk-11:latest
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
