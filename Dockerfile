FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/credit_simulator.jar /app/credit_simulator.jar
COPY file_inputs.txt /app/file_inputs.txt
ENTRYPOINT ["java", "-jar", "/app/credit_simulator.jar"]
