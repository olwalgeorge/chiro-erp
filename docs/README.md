# Chiro-ERP Documentation

## Overview

Chiro-ERP is a comprehensive, cloud-native Enterprise Resource Planning system built with modern microservices architecture, Domain-Driven Design (DDD), and Clean Architecture principles.

## 📚 Documentation Structure

### Architecture Documentation

-   [System Architecture](./architecture/system-architecture.md) - Overall system design and patterns
-   [Domain Model](./architecture/domain-model.md) - Bounded contexts and domain relationships
-   [Technology Stack](./architecture/technology-stack.md) - Technologies, frameworks, and tools
-   [Security Architecture](./architecture/security-architecture.md) - Security design and implementation

### Service Documentation

-   [Service Catalog](./services/service-catalog.md) - Complete list of microservices
-   [API Documentation](./services/api-documentation.md) - REST API specifications
-   [Event Schemas](./services/event-schemas.md) - Kafka event definitions
-   [Database Schemas](./services/database-schemas.md) - Data models per service

### Development Guide

-   [Development Setup](./development/setup.md) - Local development environment
-   [Coding Standards](./development/coding-standards.md) - Code quality and conventions
-   [Testing Strategy](./development/testing-strategy.md) - Testing approaches and tools
-   [Contributing Guide](./development/contributing.md) - How to contribute to the project

### Deployment Documentation

-   [Local Deployment](./deployment/local-deployment.md) - Running locally with Docker
-   [Kubernetes Deployment](./deployment/kubernetes-deployment.md) - Production deployment
-   [CI/CD Pipeline](./deployment/cicd-pipeline.md) - Automated deployment process
-   [Monitoring & Observability](./deployment/monitoring.md) - System monitoring setup

## 🚀 Quick Start

1. **Prerequisites**

    - Java 21+
    - Docker & Docker Compose
    - Kotlin 1.9+
    - Gradle 8+

2. **Clone Repository**

    ```bash
    git clone https://github.com/your-org/chiro-erp.git
    cd chiro-erp
    ```

3. **Start Infrastructure**

    ```bash
    docker-compose up -d postgres kafka zookeeper
    ```

4. **Build Services**

    ```bash
    ./gradlew build
    ```

5. **Run Services**
    ```bash
    ./gradlew quarkusDev
    ```

## 🏗️ System Architecture

### Microservices

| Service                                                      | Description                      | Port | Status         |
| ------------------------------------------------------------ | -------------------------------- | ---- | -------------- |
| [API Gateway](./services/api-gateway.md)                     | Entry point, routing, security   | 8080 | 🚧 Development |
| [User Management](./services/user-management.md)             | Authentication, authorization    | 8081 | 🚧 Development |
| [Tenant Management](./services/tenant-management.md)         | Multi-tenancy, configuration     | 8082 | 🚧 Development |
| [CRM Service](./services/crm-service.md)                     | Customer relationship management | 8083 | 🚧 Development |
| [Sales Service](./services/sales-service.md)                 | Sales orders, quotes, pricing    | 8084 | 🚧 Development |
| [Inventory Service](./services/inventory-service.md)         | Stock management, warehouses     | 8085 | 🚧 Development |
| [Manufacturing Service](./services/manufacturing-service.md) | Production, BOM, scheduling      | 8086 | 🚧 Development |
| [Procurement Service](./services/procurement-service.md)     | Purchase orders, vendors         | 8087 | 🚧 Development |
| [Finance Service](./services/finance-service.md)             | Accounting, GL, reporting        | 8088 | 🚧 Development |
| [Billing Service](./services/billing-service.md)             | Invoicing, payments, dunning     | 8089 | 🚧 Development |
| [HR Service](./services/hr-service.md)                       | Human resources, payroll         | 8090 | 🚧 Development |
| [Project Service](./services/project-service.md)             | Project management, timesheets   | 8091 | 🚧 Development |
| [Field Service](./services/fieldservice-service.md)          | Service requests, work orders    | 8092 | 🚧 Development |
| [Repair Service](./services/repair-service.md)               | Product repairs, warranties      | 8093 | 🚧 Development |
| [Fleet Service](./services/fleet-service.md)                 | Vehicle management, routing      | 8094 | 🚧 Development |
| [POS Service](./services/pos-service.md)                     | Point of sale, retail            | 8095 | 🚧 Development |
| [Notifications Service](./services/notifications-service.md) | Messaging, alerts                | 8096 | 🚧 Development |
| [Analytics Service](./services/analytics-service.md)         | BI, reporting, dashboards        | 8097 | 🚧 Development |

### Infrastructure Components

-   **Database**: PostgreSQL 15
-   **Message Broker**: Apache Kafka
-   **Service Discovery**: Eureka (Planned)
-   **API Gateway**: Quarkus + Vert.x
-   **Monitoring**: Prometheus + Grafana (Planned)
-   **Logging**: ELK Stack (Planned)

## 🎯 Project Status

**Current Phase**: Foundation & Architecture

-   ✅ Domain modeling and bounded contexts defined
-   ✅ Microservices structure created
-   ✅ Clean Architecture implementation
-   ✅ Infrastructure setup (Docker, Kafka, PostgreSQL)
-   🚧 Service implementation in progress
-   🚧 API contracts definition
-   📋 Testing framework setup (planned)
-   📋 CI/CD pipeline setup (planned)

## 🔄 Development Workflow

1. **Feature Development**

    - Create feature branch from `develop`
    - Implement following DDD principles
    - Write unit and integration tests
    - Update documentation
    - Submit pull request

2. **Code Quality**

    - Kotlin coding standards
    - Clean Architecture compliance
    - Domain-Driven Design patterns
    - Comprehensive testing (>80% coverage)

3. **Deployment**
    - Automated CI/CD pipeline
    - Containerized deployments
    - Blue-green deployment strategy
    - Database migrations

## 📞 Support & Contact

-   **Project Lead**: [Your Name]
-   **Email**: [your-email@company.com]
-   **Slack**: #chiro-erp-dev
-   **Issues**: [GitHub Issues](https://github.com/your-org/chiro-erp/issues)

## 📄 License

This project is licensed under the [MIT License](../LICENSE).

---

_Last updated: July 19, 2025_
