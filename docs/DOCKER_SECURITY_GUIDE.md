# Docker Security Guide for Chiro ERP

## 🔒 Security Overview

This guide addresses the security vulnerabilities found in Docker images and provides comprehensive solutions to harden your containerized Chiro ERP system.

## 🚨 Current Vulnerability Status

**Critical Issues Identified:**

-   1 Critical vulnerability
-   12 High severity vulnerabilities
-   23 Medium severity vulnerabilities
-   22 Low severity vulnerabilities

**Primary Sources:**

-   Outdated base images
-   Vulnerable system packages
-   Insecure configurations
-   Missing security controls

## 🛡️ Security Hardening Solutions

### 1. Secure Base Images

#### ❌ Problematic (Current)

```dockerfile
FROM openjdk:21-jdk-slim
FROM openjdk:21-jre-slim
```

#### ✅ Secure Alternatives

```dockerfile
# Option 1: Eclipse Temurin (Most secure OpenJDK)
FROM eclipse-temurin:21-jdk-alpine
FROM eclipse-temurin:21-jre-alpine

# Option 2: Distroless (No shell, minimal attack surface)
FROM gcr.io/distroless/java21-debian12:nonroot

# Option 3: Red Hat UBI (Enterprise security)
FROM registry.access.redhat.com/ubi9/ubi-minimal:9.4
```

### 2. Package Management Security

#### ❌ Insecure Package Installation

```dockerfile
RUN apt-get update && \
    apt-get install -y curl wget unzip
```

#### ✅ Secure Package Installation

```dockerfile
# Alpine with version pinning
RUN apk update && \
    apk add --no-cache \
        curl=8.7.1-r0 \
        wget=1.24.5-r0 \
        unzip=6.0-r14 && \
    apk upgrade --no-cache && \
    rm -rf /var/cache/apk/* /tmp/* /var/tmp/*

# Debian with specific versions
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl=7.88.1-10+deb12u4 \
        ca-certificates=20230311 && \
    apt-get upgrade -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
```

### 3. User Security

#### ❌ Running as Root

```dockerfile
# No user specified - defaults to root
```

#### ✅ Non-Root User

```dockerfile
# Alpine
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser -h /app -s /sbin/nologin

# Distroless (built-in nonroot user)
USER nonroot

# Red Hat UBI
USER 1001
```

### 4. File System Security

#### ❌ Writable Filesystem

```dockerfile
COPY app.jar /app/
```

#### ✅ Read-Only Filesystem

```dockerfile
# Set strict permissions
COPY --chown=appuser:appuser --chmod=444 app.jar /app/application.jar

# Make directories read-only
RUN chmod 555 /app

# Runtime with read-only filesystem
docker run --read-only --tmpfs /tmp myapp:secure
```

## 🔧 Secure Dockerfile Templates

### Template 1: Distroless (Most Secure)

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /build
RUN apk add --no-cache bash=5.2.21-r0 && rm -rf /var/cache/apk/*
COPY . .
RUN ./gradlew build --no-daemon

# Runtime stage - Distroless
FROM gcr.io/distroless/java21-debian12:nonroot
COPY --from=builder --chown=nonroot:nonroot /build/build/*-runner.jar /app/app.jar
USER nonroot
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Template 2: Alpine Hardened

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /build
RUN apk add --no-cache bash=5.2.21-r0 && rm -rf /var/cache/apk/*
COPY . .
RUN ./gradlew build --no-daemon

# Runtime stage - Alpine hardened
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl=8.7.1-r0 dumb-init=1.2.5-r3 && \
    apk upgrade --no-cache && \
    rm -rf /var/cache/apk/* /tmp/* /var/tmp/*

RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser -h /app

COPY --from=builder --chown=appuser:appuser --chmod=444 \
     /build/build/*-runner.jar /app/app.jar

USER appuser
HEALTHCHECK --interval=30s CMD curl -f http://localhost:8080/health || exit 1
EXPOSE 8080
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-jar", "/app/app.jar"]
```

### Template 3: UBI Micro Native

```dockerfile
FROM registry.access.redhat.com/ubi9/ubi-micro:9.4
WORKDIR /work/
RUN chown 1001:0 /work && chmod g+rwX /work
COPY --chown=1001:0 --chmod=555 build/*-runner /work/application
USER 1001
EXPOSE 8080
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
```

## 🔍 Security Scanning Integration

### 1. Local Scanning with Trivy

```powershell
# Install Trivy
# Windows (Chocolatey)
choco install trivy

# Linux/Mac
curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh

# Scan image
trivy image --severity HIGH,CRITICAL chiro-erp/service:latest

# Generate SARIF report for GitHub
trivy image --format sarif --output results.sarif chiro-erp/service:latest
```

### 2. CI/CD Integration

```yaml
# GitHub Actions security scan
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
      image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
      format: "sarif"
      output: "trivy-results.sarif"
      severity: "CRITICAL,HIGH"
      exit-code: "1" # Fail build on critical/high

- name: Upload Trivy scan results
  uses: github/codeql-action/upload-sarif@v2
  if: always()
  with:
      sarif_file: "trivy-results.sarif"
```

### 3. Automated Security Scanning Script

Use the provided `scripts/security-scanner.ps1`:

```powershell
# Scan all images
./scripts/security-scanner.ps1

# Build secure image
./scripts/security-scanner.ps1 -BuildSecure -ImageName "user-service"

# Scan specific image with detailed output
./scripts/security-scanner.ps1 -ImageName "chiro-erp/api-gateway:latest" -ScanType full -OutputFormat json
```

## 📊 Security Metrics Dashboard

### Key Security Indicators (KSIs)

| Metric                   | Target | Current | Status |
| ------------------------ | ------ | ------- | ------ |
| Critical Vulnerabilities | 0      | 1       | 🔴     |
| High Vulnerabilities     | < 5    | 12      | 🔴     |
| Medium Vulnerabilities   | < 20   | 23      | 🟡     |
| Images Using Distroless  | > 80%  | 0%      | 🔴     |
| Non-Root Containers      | 100%   | 60%     | 🟡     |
| Security Scan Coverage   | 100%   | 30%     | 🔴     |

## 🚀 Implementation Roadmap

### Phase 1: Immediate Fixes (Week 1)

-   [x] Create secure Dockerfile templates
-   [ ] Implement security scanning automation
-   [ ] Update base images to secure alternatives
-   [ ] Fix critical and high-severity vulnerabilities

### Phase 2: Enhanced Security (Week 2)

-   [ ] Implement distroless images for production
-   [ ] Add security scanning to CI/CD pipeline
-   [ ] Configure read-only filesystem
-   [ ] Implement secrets management

### Phase 3: Advanced Hardening (Week 3)

-   [ ] Security policy as code
-   [ ] Runtime security monitoring
-   [ ] Image signing and verification
-   [ ] Compliance reporting automation

## 🔒 Security Best Practices Checklist

### Build Time Security

-   [ ] ✅ Use minimal, secure base images
-   [ ] ✅ Pin specific package versions
-   [ ] ✅ Remove package managers from production images
-   [ ] ✅ Use multi-stage builds
-   [ ] ✅ Run security scans before deployment
-   [ ] ✅ Sign container images

### Runtime Security

-   [ ] ✅ Run containers as non-root users
-   [ ] ✅ Use read-only root filesystem
-   [ ] ✅ Drop unnecessary Linux capabilities
-   [ ] ✅ Use security contexts (Kubernetes)
-   [ ] ✅ Network segmentation
-   [ ] ✅ Resource limits and monitoring

### Operational Security

-   [ ] ✅ Regular base image updates
-   [ ] ✅ Automated vulnerability scanning
-   [ ] ✅ Security incident response plan
-   [ ] ✅ Access logging and monitoring
-   [ ] ✅ Secrets rotation
-   [ ] ✅ Compliance auditing

## 🛠️ Quick Fix Commands

### 1. Rebuild with Secure Images

```powershell
# Build with distroless
docker build --target runtime-distroless -f Dockerfile.multi -t chiro-erp/service:secure-distroless .

# Build with Alpine
docker build --target runtime-alpine -f Dockerfile.multi -t chiro-erp/service:secure-alpine .

# Build native
docker build --target runtime-native -f Dockerfile.multi -t chiro-erp/service:native .
```

### 2. Update Vulnerable Packages

```dockerfile
# Update package lists and upgrade
RUN apk update && apk upgrade --no-cache

# For Debian-based images
RUN apt-get update && apt-get upgrade -y && apt-get clean
```

### 3. Security Scan and Report

```powershell
# Generate security report
trivy image --format json --output security-report.json chiro-erp/service:latest

# Check for specific CVEs
trivy image --format table --severity HIGH,CRITICAL chiro-erp/service:latest | grep CVE
```

## 📚 Additional Resources

-   [Docker Security Best Practices](https://docs.docker.com/develop/security-best-practices/)
-   [NIST Container Security Guide](https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-190.pdf)
-   [CIS Docker Benchmark](https://www.cisecurity.org/benchmark/docker)
-   [OWASP Docker Security Top 10](https://github.com/OWASP/Docker-Security)

## 📞 Security Support

For security-related questions or incident reporting:

-   Create an issue with the `security` label
-   Follow responsible disclosure practices
-   Regular security reviews scheduled monthly

---

By following this guide, you can significantly reduce the security vulnerabilities in your Chiro ERP Docker images and establish a robust security posture for your containerized infrastructure.
