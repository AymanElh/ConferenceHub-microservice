# ═══════════════════════════════════════════════════════════════════
# ConferenceHub Microservices — Makefile
#
# Environments:
#   dev   → docker-compose.yml + docker-compose.dev.yml  (default)
#   prod  → docker-compose.yml + docker-compose.prod.yml
#
# Usage:
#   make help              Show this help
#   make infra-up          Start all infrastructure (dev)
#   make infra-down        Stop all infrastructure
#   make dev-tools-up      Start dev GUI tools only
#   make run-config        Run Config Service locally via Maven
# ═══════════════════════════════════════════════════════════════════

# Load environment variables from .env file
-include .env
export

# ── Compose commands ────────────────────────────────────────────
COMPOSE_DEV  = docker compose -f docker-compose.yml -f docker-compose.dev.yml
COMPOSE_PROD = docker compose -f docker-compose.yml -f docker-compose.prod.yml

# Default to dev
COMPOSE_CMD ?= $(COMPOSE_DEV)

.PHONY: help \
        infra-up infra-down \
        dev-tools-up dev-tools-down \
        keycloak-up kafka-up mongo-up postgres-up mail-up \
        run-config run-discovery run-gateway \
        run-keynote run-conference run-notification \
        build-all clean \
        prod-up prod-down

# ── Help ────────────────────────────────────────────────────────

help:
	@echo ""
	@echo "╔══════════════════════════════════════════════════════════╗"
	@echo "║         ConferenceHub — Makefile Commands                ║"
	@echo "╚══════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "┌── Infrastructure (Docker — dev env) ───────────────────────"
	@echo "│  infra-up           Start all infra (DBs, Kafka, Keycloak, mail)"
	@echo "│  infra-down         Stop all infra containers"
	@echo "│  dev-tools-up       Start dev-only GUI tools (Mongo Express,"
	@echo "│                       phpMyAdmin, Kafka UI)"
	@echo "│  dev-tools-down     Stop dev GUI tools"
	@echo "│  keycloak-up        Start Keycloak + its DB"
	@echo "│  kafka-up           Start Kafka broker"
	@echo "│  mongo-up           Start MongoDB"
	@echo "│  postgres-up        Start conference-db + keycloak-db"
	@echo "│  mail-up            Start Mailpit (SMTP trap)"
	@echo "└────────────────────────────────────────────────────────────"
	@echo ""
	@echo "┌── Microservices (local Maven) ──────────────────────────────"
	@echo "│  run-config         Config Service   → :8888"
	@echo "│  run-discovery      Discovery Service → :8761"
	@echo "│  run-gateway        Gateway Service  → :8881"
	@echo "│  run-keynote        Keynote Service  → :8081"
	@echo "│  run-conference     Conference Service → :8082"
	@echo "│  run-notification   Notification Service → :8083"
	@echo "└────────────────────────────────────────────────────────────"
	@echo ""
	@echo "┌── Build & Utilities ────────────────────────────────────────"
	@echo "│  build-all          Build all modules (skips tests)"
	@echo "│  clean              Clean all Maven target directories"
	@echo "│  prod-up            Start full stack in production mode"
	@echo "│  prod-down          Stop production stack"
	@echo "└────────────────────────────────────────────────────────────"
	@echo ""

# ── Infrastructure targets (dev) ────────────────────────────────

## Start all core infrastructure containers in dev mode
infra-up:
	$(COMPOSE_DEV) up -d \
		keycloak-db keycloak \
		keynote_db \
		kafka \
		conference-db \
		mongodb \
		mailpit

## Stop all core infrastructure containers
infra-down:
	$(COMPOSE_DEV) stop \
		keycloak-db keycloak \
		keynote_db \
		kafka \
		conference-db \
		mongodb \
		mailpit

## Start dev-only GUI tools (mongo-express, phpmyadmin, kafka-ui)
dev-tools-up:
	$(COMPOSE_DEV) up -d mongo-express phpmyadmin kafka-ui

## Stop dev-only GUI tools
dev-tools-down:
	$(COMPOSE_DEV) stop mongo-express phpmyadmin kafka-ui

## Start Keycloak and its database
keycloak-up:
	$(COMPOSE_DEV) up -d keycloak-db keycloak

## Start Kafka broker
kafka-up:
	$(COMPOSE_DEV) up -d kafka

## Start MongoDB
mongo-up:
	$(COMPOSE_DEV) up -d mongodb

## Start conference-db and keycloak-db (PostgreSQL instances)
postgres-up:
	$(COMPOSE_DEV) up -d conference-db keycloak-db

## Start Mailpit SMTP trap
mail-up:
	$(COMPOSE_DEV) up -d mailpit

# ── Local Services (Maven) ──────────────────────────────────────

run-config:
	mvn -pl config-service spring-boot:run \
	  -Dspring.cloud.config.server.git.uri=$(CONFIG_GIT_URI) \
	  -Dspring.cloud.config.server.git.username=$(CONFIG_GIT_USER) \
	  -Dspring.cloud.config.server.git.password=$(CONFIG_GIT_PASSWORD)

run-discovery:
	mvn -pl discovery-service spring-boot:run

run-gateway:
	mvn -pl gateway-service spring-boot:run

run-keynote:
	mvn -pl Keynote-service spring-boot:run

run-conference:
	mvn -pl conference-service spring-boot:run

run-notification:
	mvn -pl notification-service spring-boot:run

# ── Build & Clean ───────────────────────────────────────────────

build-all:
	mvn clean install -DskipTests

clean:
	mvn clean

# ── Production ──────────────────────────────────────────────────

prod-up:
	$(COMPOSE_PROD) up -d

prod-down:
	$(COMPOSE_PROD) down
