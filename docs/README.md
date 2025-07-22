# 📚 Chiro ERP Documentation

Welcome to the comprehensive documentation for the Chiro ERP platform. This documentation covers architecture, deployment, development, and maintenance procedures.

## 📋 Documentation Index

### 🚀 Deployment & Operations
- **[DEPLOYMENT.md](./DEPLOYMENT.md)** - Quick deployment overview and commands
- **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)** - Complete deployment guide with Docker, Kubernetes, and native compilation
- **[DEPLOYMENT_IMPROVEMENTS.md](./DEPLOYMENT_IMPROVEMENTS.md)** - Recent deployment script enhancements

### 🏗️ Architecture & Design
- **[BOUNDED_CONTEXTS_ARCHITECTURE.md](./BOUNDED_CONTEXTS_ARCHITECTURE.md)** - Domain-Driven Design and bounded contexts implementation
- **[DEPENDENCY_ARCHITECTURE.md](./DEPENDENCY_ARCHITECTURE.md)** - Detailed dependency architecture and REST communication patterns

### 📈 Recent Changes & Optimizations
- **[RECENT_OPTIMIZATIONS_SUMMARY.md](./RECENT_OPTIMIZATIONS_SUMMARY.md)** - Summary of recent build system optimizations and error resolutions

## 🏗️ System Overview

The Chiro ERP platform is a modern microservices-based Enterprise Resource Planning system built with:

### Technology Stack
- **Framework**: Quarkus 3.24.4 with new REST implementation
- **Language**: Kotlin 2.1.21 with serialization support
- **Database**: PostgreSQL with Hibernate Reactive Panache
- **Build**: Gradle 8.14 with Kotlin DSL
- **Java**: OpenJDK 21 LTS
- **Container**: Docker with Alpine Linux base images

### Service Architecture
1. **API Gateway** - Unified entry point with security and routing
2. **Core Business Service** - Finance, Sales, Procurement, Manufacturing, Inventory
3. **Customer Relations Service** - CRM and Billing
4. **Operations Management Service** - Field Service, Fleet, POS, Project Management, Repair
5. **Platform Services** - Notifications and Tenant Management
6. **Workforce Management Service** - HR and User Management

## 🚀 Quick Start

### For Deployment
```powershell
# Check system status
.\scripts\deploy-final.ps1 -Action status

# Deploy everything
.\scripts\deploy-final.ps1 -Action full
```

### For Development
```powershell
# Fix dependencies and validate structure
.\scripts\fix-dependencies.ps1
.\scripts\verify-service-structure-consistency.ps1

# Build and test
.\gradlew clean build
```

## 📁 File Organization

```
chiro-erp/
├── docs/                     # 📚 All documentation (this folder)
├── scripts/                  # 🔧 All PowerShell scripts
├── consolidated-services/    # 💼 Business service implementations
├── api-gateway/             # 🚪 API Gateway service
├── kubernetes/              # ☸️ Kubernetes manifests
├── config/                  # ⚙️ Configuration files
└── docker-compose.*.yml     # 🐳 Docker composition files
```

## 🎯 Documentation Conventions

### Document Types
- **Overview documents** (like DEPLOYMENT.md) provide quick reference and commands
- **Guide documents** (like DEPLOYMENT_GUIDE.md) provide step-by-step instructions
- **Architecture documents** explain system design and technical decisions
- **Summary documents** track changes and optimizations

### Navigation
- All documentation uses relative links for cross-references
- External links to tools and frameworks are provided where helpful
- Code examples are provided in PowerShell (Windows) format

## 🔍 Finding Information

### For Deployment Issues
1. Start with [DEPLOYMENT.md](./DEPLOYMENT.md) for quick commands
2. Check [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) for detailed procedures
3. Review [DEPLOYMENT_IMPROVEMENTS.md](./DEPLOYMENT_IMPROVEMENTS.md) for recent fixes

### For Architecture Questions
1. Review [BOUNDED_CONTEXTS_ARCHITECTURE.md](./BOUNDED_CONTEXTS_ARCHITECTURE.md) for domain design
2. Check [DEPENDENCY_ARCHITECTURE.md](./DEPENDENCY_ARCHITECTURE.md) for technical dependencies

### For Recent Changes
1. See [RECENT_OPTIMIZATIONS_SUMMARY.md](./RECENT_OPTIMIZATIONS_SUMMARY.md) for the latest updates

## 🛠️ Scripts Reference

All automation scripts are located in the `scripts/` directory:

### Deployment Scripts
- `deploy-final.ps1` - Main deployment script with multiple actions
- `k8s-deploy.ps1` - Kubernetes-specific deployment

### Maintenance Scripts
- `fix-dependencies.ps1` - Standardize Gradle dependencies
- `verify-service-structure-consistency.ps1` - Validate service structure
- `standardize-k8s-manifests.ps1` - Ensure K8s manifest consistency

### Backup & Recovery Scripts
- `create-backup.ps1` - Create system backups
- `restore-from-backup.ps1` - Restore from backups
- `backup-and-cleanup-old-services.ps1` - Clean up old service versions

### Development Scripts
- `dev-productivity.ps1` - Development environment setup
- `auto-commit.ps1` - Automated commit processes
- `check-structural-consistency.ps1` - Validate project structure

## 📞 Support & Troubleshooting

### Common Issues
- **Build failures**: Check [RECENT_OPTIMIZATIONS_SUMMARY.md](./RECENT_OPTIMIZATIONS_SUMMARY.md)
- **Deployment issues**: See troubleshooting section in [DEPLOYMENT.md](./DEPLOYMENT.md)
- **Docker problems**: Review Docker-specific guidance in [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

### System Requirements
- **Minimum**: 8GB RAM, 4 CPU cores, 10GB disk space
- **Recommended**: 16GB RAM, 8 CPU cores, 20GB disk space
- **Docker Desktop**: Latest version with WSL2 backend (Windows)

---

**This documentation is actively maintained and reflects the current state of the Chiro ERP platform as of July 2025.**

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
