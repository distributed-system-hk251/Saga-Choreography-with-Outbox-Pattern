# 🚀 Distributed System — Infra (Docker Compose)

[![Docker Compose](https://img.shields.io/badge/docker--compose-blue?logo=docker&style=flat)](https://docs.docker.com/compose/)
[![Kafka](https://img.shields.io/badge/kafka-orange?logo=apache-kafka&style=flat)](https://kafka.apache.org/)
[![Spring](https://img.shields.io/badge/spring--boot-brightgreen?logo=spring&style=flat)](https://spring.io/projects/spring-boot)

A small infra stack for the project — local development configuration using Docker Compose (KRaft-enabled Kafka, MySQL instances for each service, Eureka, Gateway).

Quick status at a glance
- ✅ Kafka (KRaft, single-node) — client: 9092
- ✅ MySQL instances: database -> host:3306
  - Database:
    - orderdb
    - paymentdb
    - productdb
    - notificationdb
- ✅ Eureka registry -> 8761
- ✅ API Gateway -> 8080

Get started (Windows / PowerShell)
1. From repo root:
   - docker compose -f infra/docker-compose.yaml up -d
2. Follow logs:
   - docker compose -f infra/docker-compose.yaml logs -f kafka
   - docker compose -f infra/docker-compose.yaml logs -f orderdb

Verify Kafka topics
- Using the Kafka container (container name `broker` / service `kafka`):
  - docker exec -it broker bash -c "kafka-topics.sh --bootstrap-server localhost:9092 --list"
  - If using a different image (bitnami), adjust path or use the container shell to run kafka-topics.sh.

Order Service — connection hints
- When running Order Service inside the same Docker Compose network:
  - KAFKA bootstrap: kafka:9092
  - app.kafka.topic: order-topic
  - MySQL JDBC URL (container/local): jdbc:mysql://localhost:3306/orderdb (compose maps host port 3306)
- If running the service locally (outside Docker), set env:
  - KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  - SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/orderdb?useSSL=false&serverTimezone=UTC

Common commands
- Stop infra:
  - docker compose -f infra/docker-compose.yaml down
- Restart a single service:
  - docker compose -f infra/docker-compose.yaml restart kafka
- Inspect container shell:
  - docker exec -it broker /bin/bash

Troubleshooting
- Kafka not reachable from local app:
  - Ensure kafka service mapped port 9092 is open.
  - If app runs inside Docker, use service name `kafka:9092` not `localhost`.
- Topic not present:
  - Create on startup by setting KAFKA_CREATE_TOPICS in compose, or create manually with kafka-topics.sh.
- MySQL connection issues:
  - Confirm mapped host port (orderdb -> 3306) and credentials in application.yaml.

Notes
- This compose file uses a single-node KRaft (no ZooKeeper). Exposed controller port (9093) is optional.
- Keep secrets out of source control for production — use env files or secret manager.

Need the README styled differently (more colors, badges, or a diagram)? I can add a minimal ASCII diagram or SVG and update the file.
```// filepath: c:\Users\MyClone\OneDrive\Desktop\Project\Distributed_System\infra\README.md
# 🚀 Distributed System — Infra (Docker Compose)

[![Docker Compose](https://img.shields.io/badge/docker--compose-blue?logo=docker&style=flat)](https://docs.docker.com/compose/)
[![Kafka](https://img.shields.io/badge/kafka-orange?logo=apache-kafka&style=flat)](https://kafka.apache.org/)
[![Spring](https://img.shields.io/badge/spring--boot-brightgreen?logo=spring&style=flat)](https://spring.io/projects/spring-boot)

A small infra stack for the project — local development configuration using Docker Compose (KRaft-enabled Kafka, MySQL instances for each service, Eureka, Gateway).

Quick status at a glance
- ✅ Kafka (KRaft, single-node) — client: 9092
- ✅ MySQL instances:
  - order-service-db -> host:3306
  - payment-service-db -> host:3307
  - product-service-db -> host:3308
  - notification-service-db -> host:3309
- ✅ Eureka registry -> 8761
- ✅ API Gateway -> 8080

Get started (Windows / PowerShell)
1. From repo root:
   - docker compose -f infra/docker-compose.yaml up -d
2. Follow logs:
   - docker compose -f infra/docker-compose.yaml logs -f kafka
   - docker compose -f infra/docker-compose.yaml logs -f orderdb

Verify Kafka topics
- Using the Kafka container (container name `broker` / service `kafka`):
  - docker exec -it broker bash -c "kafka-topics.sh --bootstrap-server localhost:9092 --list"
  - If using a different image (bitnami), adjust path or use the container shell to run kafka-topics.sh.

Order Service — connection hints
- When running Order Service inside the same Docker Compose network:
  - KAFKA bootstrap: kafka:9092
  - app.kafka.topic: order-topic
  - MySQL JDBC URL (container/local): jdbc:mysql://localhost:3306/orderdb (compose maps host port 3306)
- If running the service locally (outside Docker), set env:
  - KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  - SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/orderdb?useSSL=false&serverTimezone=UTC

Common commands
- Stop infra:
  - docker compose -f infra/docker-compose.yaml down
- Restart a single service:
  - docker compose -f infra/docker-compose.yaml restart kafka
- Inspect container shell:
  - docker exec -it broker /bin/bash

Troubleshooting
- Kafka not reachable from local app:
  - Ensure kafka service mapped port 9092 is open.
  - If app runs inside Docker, use service name `kafka:9092` not `localhost`.
- Topic not present:
  - Create on startup by setting KAFKA_CREATE_TOPICS in compose, or create manually with kafka-topics.sh.
- MySQL connection issues:
  - Confirm mapped host port (orderdb -> 3306) and credentials in application.yaml.

Notes
- This compose file uses a single-node KRaft (no ZooKeeper). Exposed controller port (9093) is optional.
- Keep secrets out of source control for production — use env files or secret manager.

Need the README styled differently (more colors, badges, or a diagram)? I can add a minimal ASCII diagram or SVG and update the file.