# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy root pom (dependency management)
COPY pom.xml ./pom.xml

# Copy only this service's pom for layer caching
COPY discovery-service/pom.xml ./discovery-service/pom.xml

# Download deps only (cached if pom unchanged)
RUN mvn dependency:go-offline -pl discovery-service -am --batch-mode

# Copy source
COPY discovery-service/src ./discovery-service/src

# Build only this module
RUN mvn clean package -pl discovery-service -DskipTests --batch-mode

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/discovery-service/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
