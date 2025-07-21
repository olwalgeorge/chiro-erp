#!/bin/bash

# Define the root project directory
PROJECT_ROOT="chiro-erp"

# Function to create a directory if it doesn't exist
create_dir() {
    local dir_path="$1"
    if [ ! -d "$dir_path" ]; then
        echo "Creating directory: $dir_path"
        mkdir -p "$dir_path"
    else
        echo "Directory already exists: $dir_path (Skipping)"
    fi
}

# Function to create a file if it doesn't exist
create_file() {
    local file_path="$1"
    local content="$2" # Optional content for the file
    if [ ! -f "$file_path" ]; then
        echo "Creating file: $file_path"
        if [ -n "$content" ]; then
            echo "$content" > "$file_path"
        else
            touch "$file_path"
        fi
    else
        echo "File already exists: $file_path (Skipping)"
    fi
}

echo "Starting Chiro ERP project structure creation..."

# Create the root project directory
create_dir "$PROJECT_ROOT"
cd "$PROJECT_ROOT" || exit

# --- Top-level files and directories ---
create_dir "buildSrc/src/main/kotlin/org/chiro"
create_file "buildSrc/src/main/kotlin/org/chiro/common-conventions.gradle.kts" "# Common Quarkus Kotlin conventions"
create_file ".editorconfig" "# EditorConfig settings"
create_file ".gitignore" "# Git ignore rules"
create_file "build.gradle.kts" "# Root build script"
create_dir "gradle/wrapper"
create_file "gradle/wrapper/gradle-wrapper.jar" "" # Placeholder for the actual jar
create_file "gradle/wrapper/gradle-wrapper.properties" "# Gradle wrapper properties"
create_file "gradlew" "# Gradle wrapper script"
create_file "gradlew.bat" "# Gradle wrapper batch script"
create_file "settings.gradle.kts" "# Gradle settings script"
create_file "docker-compose.yml" "# Docker Compose for local development"
create_file "Dockerfile" "# Centralized Dockerfile for all services"
create_file ".dockerignore" "# Docker ignore rules"
create_file "README.md" "# Project README"

# --- Kubernetes directory structure ---
create_dir "kubernetes/base"
create_file "kubernetes/base/kustomization.yml" "# Base Kustomization"
create_file "kubernetes/base/namespace.yml" "# Namespace definition"

create_dir "kubernetes/platform"
create_file "kubernetes/platform/kafka-cluster.yml" "# Kafka cluster definition"
create_file "kubernetes/platform/postgres-db.yml" "# PostgreSQL database definition"
create_file "kubernetes/platform/prometheus-stack.yml" "# Prometheus monitoring stack"
create_file "kubernetes/platform/grafana-dashboards.yml" "# Grafana dashboards"
create_file "kubernetes/platform/logging-stack.yml" "# Centralized logging stack (e.g., Loki/Fluentd)"

create_dir "kubernetes/ingress"
create_file "kubernetes/ingress/ingress.yml" "# Ingress controller configuration"

# --- Kubernetes Service Deployments (Original granular services, for K8s manifests) ---
K8S_SERVICES=(
    "finance-service"
    "inventory-service"
    "sales-service"
    "procurement-service"
    "fleet-service"
    "project-service"
    "manufacturing-service"
    "fieldservice-service"
    "repair-service"
    "hr-service"
    "tenant-management-service"
    "user-management-service"
    "billing-service" # Original billing service (might be renamed in code, but K8s manifest might still exist)
    "pos-service"
    "crm-service"
    "api-gateway"
    "analytics-service"
    "notifications-service"
    "platform-subscription-billing-service" # New platform billing K8s manifest
)

for svc in "${K8S_SERVICES[@]}"; do
    create_dir "kubernetes/services/$svc"
    create_file "kubernetes/services/$svc/deployment.yml" "# Deployment for $svc"
    create_file "kubernetes/services/$svc/service.yml" "# Service for $svc"
done

# --- Kafka Topics ---
create_dir "kubernetes/kafka"
create_file "kubernetes/kafka/kafka-cluster.yml" "# Kafka Cluster setup"

KAFKA_TOPICS=(
    "finance-events"
    "inventory-events"
    "sales-events"
    "procurement-events"
    "fleet-events"
    "project-events"
    "manufacturing-events"
    "fieldservice-events"
    "repair-events"
    "hr-events"
    "tenant-events"
    "user-events"
    "platform-billing-events" # Events for platform subscriptions/usage
    "erp-invoicing-events" # Events for ERP-generated invoices (from Customer-Sales)
    "crm-events"
    "analytics-events"
    "notifications-events"
    "product-events" # General product data/price changes
)

for topic in "${KAFKA_TOPICS[@]}"; do
    create_file "kubernetes/kafka/topic-$topic.yml" "# Kafka Topic: $topic"
done

create_dir "kubernetes/postgresql"
create_file "kubernetes/postgresql/postgres-db.yml" "# PostgreSQL database K8s manifest"


# --- Consolidated Microservices Codebases ---
create_dir "services"

CONSOLIDATED_SERVICES=(
    "customer-sales-management-service"
    "financial-core-service"
    "supply-chain-operations-service"
    "hr-workforce-service"
    "service-maintenance-management-service"
    "api-gateway"
    "tenant-management-service"
    "user-management-service"
    "notifications-service"
    "analytics-service"
    "platform-subscription-billing-service"
)

for svc in "${CONSOLIDATED_SERVICES[@]}"; do
    create_dir "services/$svc"
    # Placeholder for the main Kotlin source directory
    create_dir "services/$svc/src/main/kotlin/org/chiro/${svc//-/_}" # Convert hyphen to underscore for package name
    create_file "services/$svc/build.gradle.kts" "// Gradle build script for $svc"
    create_file "services/$svc/README.md" "# $svc Microservice"
    create_dir "services/$svc/src/main/resources"
    create_file "services/$svc/src/main/resources/application.properties" "# Quarkus application properties for $svc"
    create_dir "services/$svc/src/main/resources/db/migration"
    create_file "services/$svc/src/main/resources/db/migration/.gitkeep" "" # Placeholder for Flyway/Liquibase scripts
    create_dir "services/$svc/src/test/kotlin/org/chiro/${svc//-/_}"
    create_dir "services/$svc/src/test/resources"
done

echo "Chiro ERP project structure creation complete!"