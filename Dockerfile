# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy the Gradle wrapper and build config first.
# These change rarely, so Docker can cache the layers below
# them and skip re-downloading dependencies on every code change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Copy source and build the executable jar.
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# curl is used only by the container healthcheck.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as an unprivileged user, never as root.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

# Copy just the built jar from the build stage — none of the
# source, Gradle, or JDK comes along.
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]