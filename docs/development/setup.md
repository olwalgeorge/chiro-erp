# Development Setup Guide

## Prerequisites

### Required Software

| Software           | Version  | Purpose                 | Installation                                                      |
| ------------------ | -------- | ----------------------- | ----------------------------------------------------------------- |
| **Java**           | 21 (LTS) | Runtime platform        | [Download OpenJDK](https://openjdk.org/)                          |
| **Kotlin**         | 1.9+     | Programming language    | Bundled with Gradle                                               |
| **Gradle**         | 8.0+     | Build tool              | [Installation Guide](https://gradle.org/install/)                 |
| **Docker**         | 24+      | Containerization        | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| **Docker Compose** | 2.0+     | Container orchestration | Included with Docker Desktop                                      |
| **Git**            | 2.40+    | Version control         | [Git Downloads](https://git-scm.com/downloads)                    |

### Optional but Recommended

| Software          | Purpose         | Installation                                           |
| ----------------- | --------------- | ------------------------------------------------------ |
| **IntelliJ IDEA** | Primary IDE     | [JetBrains](https://www.jetbrains.com/idea/)           |
| **Postman**       | API testing     | [Postman Download](https://www.postman.com/downloads/) |
| **DBeaver**       | Database client | [DBeaver Download](https://dbeaver.io/download/)       |

## Initial Setup

### 1. Clone Repository

```bash
git clone https://github.com/your-org/chiro-erp.git
cd chiro-erp
```

### 2. Environment Configuration

Create environment files for different profiles:

#### Development Environment

```bash
# Create .env.dev file
cat << EOF > .env.dev
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=chiro_erp_dev
DB_USER=chiro
DB_PASSWORD=chiro123

# Kafka
KAFKA_BROKERS=localhost:9092
ZOOKEEPER_CONNECT=localhost:2181

# Application
ENVIRONMENT=development
LOG_LEVEL=DEBUG
JWT_SECRET=your-jwt-secret-key-for-development
EOF
```

#### Test Environment

```bash
# Create .env.test file
cat << EOF > .env.test
# Database
DB_HOST=localhost
DB_PORT=5433
DB_NAME=chiro_erp_test
DB_USER=chiro_test
DB_PASSWORD=chiro_test123

# Kafka
KAFKA_BROKERS=localhost:9093
ZOOKEEPER_CONNECT=localhost:2182

# Application
ENVIRONMENT=test
LOG_LEVEL=INFO
JWT_SECRET=test-jwt-secret-key
EOF
```

### 3. Infrastructure Setup

Start the required infrastructure services:

```bash
# Start infrastructure services
docker-compose up -d postgres kafka zookeeper

# Verify services are running
docker-compose ps

# Check logs if needed
docker-compose logs -f postgres
docker-compose logs -f kafka
```

### 4. Database Setup

```bash
# Create databases for each service
docker exec -it chiro-erp-postgres-1 psql -U chiro -d chiro_erp

# Run this for each service database
CREATE DATABASE chiro_crm;
CREATE DATABASE chiro_sales;
CREATE DATABASE chiro_inventory;
CREATE DATABASE chiro_manufacturing;
CREATE DATABASE chiro_procurement;
CREATE DATABASE chiro_finance;
CREATE DATABASE chiro_billing;
CREATE DATABASE chiro_hr;
CREATE DATABASE chiro_project;
CREATE DATABASE chiro_fieldservice;
CREATE DATABASE chiro_repair;
CREATE DATABASE chiro_fleet;
CREATE DATABASE chiro_pos;
CREATE DATABASE chiro_user_management;
CREATE DATABASE chiro_tenant_management;
CREATE DATABASE chiro_notifications;
CREATE DATABASE chiro_analytics;
```

### 5. Build Configuration

Update the root `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "chiro-erp"

// Include all services
include("api-gateway")
include("services:user-management-service")
include("services:tenant-management-service")
include("services:crm-service")
include("services:sales-service")
include("services:inventory-service")
include("services:manufacturing-service")
include("services:procurement-service")
include("services:finance-service")
include("services:billing-service")
include("services:hr-service")
include("services:project-service")
include("services:fieldservice-service")
include("services:repair-service")
include("services:fleet-service")
include("services:pos-service")
include("services:notifications-service")
include("services:analytics-service")

// Common modules
include("common:common-domain")
include("common:common-infrastructure")
include("common:api-contracts")
```

### 6. Build Project

```bash
# Clean and build all services
./gradlew clean build

# Build specific service
./gradlew :services:crm-service:build

# Run tests
./gradlew test

# Generate test reports
./gradlew jacocoTestReport
```

## Development Workflow

### 1. Service Development

```bash
# Start a service in development mode
./gradlew :services:crm-service:quarkusDev

# The service will be available at:
# http://localhost:8083

# Development UI will be available at:
# http://localhost:8083/q/dev/
```

### 2. API Testing

Use the provided Postman collection or curl commands:

```bash
# Health check
curl http://localhost:8083/q/health

# Create a customer (example)
curl -X POST http://localhost:8083/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corp",
    "email": "contact@acme.com",
    "phone": "+1-555-123-4567"
  }'
```

### 3. Database Development

```bash
# Access database
docker exec -it chiro-erp-postgres-1 psql -U chiro -d chiro_crm

# View database schema
\dt

# Run migrations (when implemented)
./gradlew :services:crm-service:flywayMigrate
```

### 4. Event Testing

```bash
# Kafka console consumer (monitor events)
docker exec -it chiro-erp-kafka-1 kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic chiro.dev.crm.customer.created \
  --from-beginning

# Kafka console producer (send test events)
docker exec -it chiro-erp-kafka-1 kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic chiro.dev.crm.customer.created
```

## IDE Setup

### IntelliJ IDEA Configuration

1. **Import Project**

    - Open IntelliJ IDEA
    - File → Open → Select `chiro-erp` directory
    - Choose "Import Gradle project"

2. **Configure Kotlin**

    - File → Settings → Languages & Frameworks → Kotlin
    - Set Kotlin compiler version to 1.9+
    - Enable "Auto-configure" for Kotlin/JVM

3. **Database Configuration**

    - View → Tool Windows → Database
    - Add PostgreSQL data source:
        - Host: localhost
        - Port: 5432
        - Database: chiro_erp_dev
        - User: chiro
        - Password: chiro123

4. **Run Configurations**
   Create run configurations for each service:
    ```
    Name: CRM Service
    Main class: io.quarkus.runner.GeneratedMain
    Module: services.crm-service.main
    Environment variables:
      QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/chiro_crm
      QUARKUS_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
    ```

### VS Code Configuration

1. **Extensions**

    - Kotlin Language Support
    - Gradle for Java
    - Docker
    - REST Client

2. **Settings**
    ```json
    {
        "java.home": "/path/to/java-21",
        "kotlin.languageServer.enabled": true,
        "gradle.nestedProjects": true
    }
    ```

## Testing Strategy

### Unit Tests

```kotlin
// Example unit test
@QuarkusTest
class CustomerServiceTest {

    @Inject
    lateinit var customerService: CustomerService

    @Test
    fun `should create customer successfully`() {
        // Given
        val command = CreateCustomerCommand(
            name = "Test Customer",
            email = "test@example.com"
        )

        // When
        val result = customerService.createCustomer(command)

        // Then
        assertThat(result.name).isEqualTo("Test Customer")
        assertThat(result.email).isEqualTo("test@example.com")
    }
}
```

### Integration Tests

```kotlin
// Example integration test
@QuarkusTest
@TestProfile(IntegrationTestProfile::class)
class CustomerResourceIT {

    @Test
    fun `should create and retrieve customer`() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "name": "Integration Test Customer",
                    "email": "integration@test.com"
                }
            """)
        .`when`()
            .post("/api/customers")
        .then()
            .statusCode(201)
            .body("name", equalTo("Integration Test Customer"))
    }
}
```

### Test Containers

```kotlin
// Example test container setup
@QuarkusTest
@TestProfile(TestContainerProfile::class)
class CustomerRepositoryIT {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }
}
```

## Debugging

### Application Debugging

```bash
# Start service with debug mode
./gradlew :services:crm-service:quarkusDev -Ddebug=5005

# Connect IntelliJ debugger to port 5005
```

### Database Debugging

```bash
# Enable SQL logging in application.yml
quarkus:
  hibernate-orm:
    log:
      sql: true
      format-sql: true
```

### Kafka Debugging

```bash
# List topics
docker exec chiro-erp-kafka-1 kafka-topics.sh --list --bootstrap-server localhost:9092

# Describe topic
docker exec chiro-erp-kafka-1 kafka-topics.sh --describe --topic chiro.dev.crm.customer.created --bootstrap-server localhost:9092

# Consumer groups
docker exec chiro-erp-kafka-1 kafka-consumer-groups.sh --list --bootstrap-server localhost:9092
```

## Common Issues & Solutions

### Issue: Port Already in Use

```bash
# Find process using port
lsof -i :8080
# Or on Windows
netstat -ano | findstr :8080

# Kill process
kill -9 <PID>
```

### Issue: Database Connection Failed

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Restart service
docker-compose restart postgres
```

### Issue: Kafka Not Receiving Messages

```bash
# Check Kafka logs
docker-compose logs kafka

# Verify topic exists
docker exec chiro-erp-kafka-1 kafka-topics.sh --list --bootstrap-server localhost:9092

# Create topic if missing
docker exec chiro-erp-kafka-1 kafka-topics.sh --create --topic your-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

## Performance Tips

### JVM Optimization

```bash
# Set JVM options for development
export GRADLE_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"
export JAVA_OPTS="-Xms1g -Xmx2g"
```

### Database Optimization

```sql
-- Create indexes for frequent queries
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
```

### Build Optimization

```bash
# Use Gradle build cache
./gradlew build --build-cache

# Parallel builds
./gradlew build --parallel
```

---

_This development setup provides a solid foundation for productive development of the Chiro-ERP system._
