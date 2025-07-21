# Dependency Standardization Summary
Generated: 07/21/2025 17:23:55

## Reference Project Alignment
- Source: https://github.com/olwalgeorge/erp
- Quarkus Version: 3.24.4 (upgraded from reference 3.24.3)
- Kotlin Version: 2.1.21
- Java Version: 21

## Key Standardizations Applied

### 1. REST API Strategy (HYBRID BEST PRACTICE)
- **Internal APIs**: Kotlin Serialization (quarkus-rest-kotlin-serialization) - Type-safe, compile-time validation
- **External APIs**: Jackson (quarkus-rest-jackson) - Ecosystem compatibility, enterprise integrations
- **Core REST**: quarkus-rest (from reference project)
- **Benefits**: Best of both worlds - type safety for internal, compatibility for external

### 2. Database Layer
- **Core ORM**: quarkus-hibernate-orm (from reference)
- **Kotlin Integration**: quarkus-hibernate-orm-panache-kotlin
- **Database**: PostgreSQL (quarkus-jdbc-postgresql from reference)

### 3. BOM Management
- Using nforcedPlatform (from reference project)
- All Quarkus dependencies use BOM versions
- Only non-BOM dependencies specify versions explicitly

### 4. Plugin Management
- Added pluginManagement block to settings.gradle.kts (from reference)
- Centralized plugin versions in gradle.properties

### 5. Build Configuration
- Java 21 target (from reference)
- UTF-8 encoding (from reference)
- Compiler parameters flag (from reference)

## Convention Plugin Hierarchy
1. **common-conventions**: Base layer with core dependencies
2. **service-conventions**: Service-specific additions
3. **quality-conventions**: Code quality tools
4. **consolidated-service-conventions**: Top layer for main services

## Testing Strategy
- JUnit 5 (quarkus-junit5 from reference)
- REST Assured (est-assured from reference) 
- Kotlin Test support
- Testcontainers for integration tests
