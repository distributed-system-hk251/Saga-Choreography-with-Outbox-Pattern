# --- Development Image ---
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Use cached dependencies for faster rebuilds
COPY pom.xml .
RUN mkdir -p /app/src/main/java
RUN ./mvnw dependency:go-offline || true

# Copy source code (hot-reload with mounted volume)
COPY . .


CMD ["./mvnw", "spring-boot:run"]
