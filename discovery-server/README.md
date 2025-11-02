# 📦 Eureka Server

Eureka Server is a **service discovery server** in a distributed system.  
It allows other services (Eureka Clients) to **register themselves** and **discover each other** without manually configuring service addresses.

---

## 🚀 Running the Project

### 1. Environment Requirements
- **Java 21+**  
- **Maven 3.9+** or **Gradle**  
- **Spring Boot 3.x**  
- Git (if running from a separate repository)

### 2. Configuration
The main configuration file:
- `src/main/resources/application.yml`

You can override settings using profiles such as:
- `application-dev.yml`  
- `application-prod.yml`

Example basic configuration in `application.yml`:

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```
