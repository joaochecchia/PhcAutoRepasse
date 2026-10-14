FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
RUN ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app app \
    && mkdir -p /app/uploads /app/logs \
    && chown -R app:app /app/uploads /app/logs
COPY --from=build --chown=app:app /app/target/backend-*.jar /app/backend.jar

USER app
EXPOSE 8080 9091
ENTRYPOINT ["java", "-jar", "/app/backend.jar"]
