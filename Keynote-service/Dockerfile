FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY config-service/pom.xml config-service/pom.xml
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY Keynote-service/pom.xml Keynote-service/pom.xml
COPY conference-service/pom.xml conference-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -pl Keynote-service -am dependency:go-offline --batch-mode

COPY . .

RUN mvn -pl Keynote-service -am clean package -DskipTests --batch-mode

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ARG BUILD_VERSION=unknown
LABEL org.opencontainers.image.title="conferencehub-keynote" \
      org.opencontainers.image.version="${BUILD_VERSION}" \
      org.opencontainers.image.source="https://github.com/aymanelh/ConferenceHub-microservices"

RUN apk add --no-cache wget \
    && addgroup -S spring \
    && adduser -S spring -G spring
USER spring:spring

COPY --from=builder --chown=spring:spring /workspace/Keynote-service/target/*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
