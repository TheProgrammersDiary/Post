FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY post/target/post*.jar /app/post.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/post.jar"]