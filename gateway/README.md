# 📦 API Gateway Service

API Gateway is a microservice that acts as a **single entry point** for all client requests in the distributed system.  
It handles **routing, load balancing, authentication, and request filtering** before forwarding requests to downstream microservices.  
Built with **Spring Boot** and **Spring Cloud Gateway**, it integrates with **Eureka Server** for service discovery.

---

## 🚀 Features
- Route requests to appropriate microservices.  
- Service discovery integration via **Eureka**.  
- Request filtering and logging.  
- Authentication and authorization support (e.g., JWT).  
- Rate limiting and resilience policies.  

---

## ⚙️ Configuration

### 1. Default Configuration (`application.yml`)
```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
        - id: payment-service
          uri: lb://PAYMENT-SERVICE
          predicates:
            - Path=/api/payments/**
```
