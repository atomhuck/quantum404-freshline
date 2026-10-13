FROM maven:3.9.13-eclipse-temurin-21-noble AS build
WORKDIR /workspace
COPY backend/ backend/
RUN mvn -B -f backend/pom.xml -pl app -am package -DskipTests

FROM eclipse-temurin:21-jre-ubi10-minimal
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/backend/app/target/freshline-app-0.1.0-SNAPSHOT.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
