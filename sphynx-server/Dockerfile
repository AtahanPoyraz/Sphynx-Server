FROM openjdk:24-slim AS jdk
RUN apt-get update && apt-get install -y maven && apt-get install -y curl

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:24-slim

WORKDIR /app
COPY --from=jdk /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

EXPOSE 8080
EXPOSE 50051