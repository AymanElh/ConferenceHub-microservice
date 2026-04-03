# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/
WORKDIR /build/discovery-service
RUN mvn dependency:go-offline

COPY discovery-service/src src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/discovery-service/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
