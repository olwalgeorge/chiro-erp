# Multi-stage build for optimized JVM mode with GraalVM
FROM quay.io/quarkus/ubi-quarkus-graalvm-builder-image:jdk-17 AS build

WORKDIR /code
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY buildSrc/ buildSrc/
COPY consolidated-services/core-business-service/ consolidated-services/core-business-service/

# Build optimized JVM application
RUN ./gradlew :consolidated-services:core-business-service:build

# Runtime stage with GraalVM JIT
FROM registry.access.redhat.com/ubi8/openjdk-17:1.15

ENV LANGUAGE='en_US:en'

# Optimized layer copying
COPY --from=build --chown=185 /code/consolidated-services/core-business-service/build/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /code/consolidated-services/core-business-service/build/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /code/consolidated-services/core-business-service/build/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /code/consolidated-services/core-business-service/build/quarkus-app/quarkus/ /deployments/quarkus/

# GraalVM optimizations
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/q/health || exit 1

EXPOSE 8080
USER 185

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
