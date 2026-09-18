FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

RUN chmod +x mvnw && ./mvnw -B clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/lauradefreitas2/policy-billing-engine"
LABEL org.opencontainers.image.description="Recurring mobile insurance policy billing engine"

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/target/policy-billing-engine-*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
