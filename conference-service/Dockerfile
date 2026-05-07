# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy root pom (dependency management)
COPY pom.xml ./pom.xml

# Copy only this service's pom for layer caching
COPY conference-service/pom.xml ./conference-service/pom.xml

# Download deps only (cached if pom unchanged)
RUN mvn dependency:go-offline -pl conference-service -am --batch-mode

# Copy source
COPY conference-service/src ./conference-service/src

# Build only this module
RUN mvn clean package -pl conference-service -DskipTests --batch-mode

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/conference-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
