# Stage 1: Build
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app

COPY ../pom.xml .
RUN mvn dependency:go-offline

COPY .. .
RUN mvn clean package -DskipTests

# Stage 2: Production
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
