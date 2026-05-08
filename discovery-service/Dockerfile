# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy parent POM and this module POM first for dependency layer caching
COPY pom.xml ./pom.xml
COPY discovery-service/pom.xml ./discovery-service/pom.xml

WORKDIR /workspace/discovery-service
RUN mvn dependency:go-offline --batch-mode

# Copy source
COPY discovery-service/src ./src

RUN mvn clean package -DskipTests --batch-mode

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder --chown=spring:spring /workspace/discovery-service/target/*.jar app.jar

EXPOSE 8761

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
