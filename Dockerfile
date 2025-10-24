# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /project

COPY pom.xml /project/pom.xml
RUN mvn dependency:go-offline

COPY src /project/src
RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:17-jre
WORKDIR /app

ENV SERVICE_NAME=testlab
ENV JAR_FILE=${SERVICE_NAME}.jar
ENV APP_ENV=local

COPY --from=builder /project/target/${SERVICE_NAME}/${SERVICE_NAME}*-fat.jar /app/${JAR_FILE}
COPY --from=builder /project/target/${SERVICE_NAME}/resources/logback/logback.xml /app/logback.xml

EXPOSE 8080

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar -Dapp.environment=${APP_ENV} -Dlogback.configurationFile=/app/logback.xml /app/${JAR_FILE}"]
