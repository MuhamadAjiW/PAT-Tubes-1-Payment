FROM openjdk:17-jdk-slim
WORKDIR /app
COPY ./target/piss-1.0.war ./app.war
EXPOSE 8080
CMD ["java", "-jar", "app.war"]