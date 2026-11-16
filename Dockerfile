# ---- Stage 1: build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Gradle wrapper and build files first so dependencies get cached
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

# Fix line endings / permissions in case the repo was checked out on Windows
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Build the application (resources incl. models/ and native.linux-x64/ get packed in the jar)
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---- Stage 2: runtime ----
# Debian/Ubuntu-based (glibc) image: needed for the bundled .so native libraries
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends libgomp1 \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r spring && useradd -r -g spring -m spring

# NEW: secrets directory owned by the app user, plus the entrypoint script
RUN mkdir -p /etc/secrets && chown spring:spring /etc/secrets
COPY entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/src/main/resources/ca.pem /app/src/main/resources/ca.pem

EXPOSE 8080

# CHANGED: the script now starts java
ENTRYPOINT ["/entrypoint.sh"]