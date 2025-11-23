FROM maven:3.9.11-eclipse-temurin-17-noble AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/target/lyra-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]