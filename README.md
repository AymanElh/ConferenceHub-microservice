# 📽️ ConferenceHub - Microservices Platform

[![Microservices](https://img.shields.io/badge/Architecture-Microservices-orange?style=for-the-badge)](https://microservices.io/)
[![Spring Boot](https://img.shields.io/badge/Spring--Boot-4.0.3-brightgreen?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring--Cloud-2025.1.1-blue?style=for-the-badge&logo=spring-cloud)](https://spring.io/projects/spring-cloud)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](https://opensource.org/licenses/MIT)

**ConferenceHub** is a state-of-the-art, event-driven microservices platform designed to orchestrate conferences, manage keynotes, and handle real-time notifications. Built with high availability and scalability in mind, it leverages the latest Spring Cloud ecosystem and asynchronous event processing with Apache Kafka.

---

## 🏗️ Architecture Overview

The system is composed of several specialized microservices, each responsible for a specific domain, ensuring a decoupled and resilient architecture.


### 🧩 Core Services

| Service | Description | Tech Stack |
|:---|:---|:---|
| **`config-service`** | Centralized configuration management using Spring Cloud Config. | Git-backed config |
| **`discovery-service`** | Service registration and discovery with Netflix Eureka. | Eureka Server |
| **`gateway-service`** | API Gateway for routing, load balancing, and edge security. | Spring Cloud Gateway |
| **`conference-service`** | Core domain service for managing conferences, inscriptions, and reviews. | PostgreSQL, JPA |
| **`keynote-service`** | Management of speakers (Keynotes) and their assignments. | MariaDB, Feign |
| **`notification-service`** | Event-driven notification system for email alerts. | Kafka, MongoDB, Mailpit |

---

## 🚀 Key Features

### 🔐 Security & Resilience
- **Identity Management**: Integrated with **Keycloak** (OAuth2/OIDC) for centralized authentication and role-based access control.
- **RBAC Roles**:
  - **`PARTICIPANT`** (default): can browse conferences/keynotes, register to conferences, and add reviews.
  - **`ADMIN`**: can manage (create/update) conferences, view inscriptions, and create/update/delete keynotes.
- **Graceful Degradation**: Implements **Resilience4j** circuit breakers and fallback mechanisms to ensure system stability during partial failures.

### 📡 Event-Driven Excellence
- **Asynchronous Workflows**: High-throughput event processing using **Apache Kafka** for cross-service communication (e.g., conference status updates, review notifications).
- **History Tracking**: Durable notification history stored in **MongoDB**.

### 🔍 Observability & CI/CD
- **Distributed Tracing**: Full request lifecycle visibility across services using **Zipkin** and Brave.
- **Metric Monitoring**: Real-time performance tracking with **Prometheus** and Spring Boot Actuator.
- **Continuous Integration**: Fully automated **Jenkins** pipeline for building, testing, and containerizing each microservice.

### 📚 Documentation
- **Self-Documenting API**: Interactive and always up-to-date documentation via **SpringDoc OpenAPI (Swagger)**.

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 4.0.3, Spring Cloud 2025.1.1
- **Communication**: REST (Feign), Apache Kafka (Events)
- **Database**: PostgreSQL, MariaDB, MongoDB
- **Security**: Keycloak
- **Observability**: Zipkin, Prometheus, Micrometer
- **DevOps**: Docker, Jenkins
- **Testing**: JUnit 5, Mockito, Testcontainers, Mailpit (SMTP Mock)

---

## 🚦 Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- Java 17+
- Maven

### Running the Platform

To spin up the infrastructure and all microservices:

```bash
docker-compose up -d
```

### Accessing Services

- **Gateway**: `http://localhost:8080`
- **Eureka Dashboard**: `http://localhost:8761`
- **Keycloak Console**: `http://localhost:8180`
- **Mailpit Dashboard**: `http://localhost:8025`
- **Swagger Documentation**: Available at `[service-url]/swagger-ui.html`

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Developed with ❤️ by Ayman Elhamioui*
