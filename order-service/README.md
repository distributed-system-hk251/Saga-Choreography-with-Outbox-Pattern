# 📦 Order Service

Order Service is a microservice in a distributed system, responsible for managing **orders**.  
It is built with **Spring Boot**, connects to **databases** (MySQL/H2), and registers with **Eureka Server** for service discovery.  
Additionally, it can dynamically load configuration from **Spring Cloud Config Server**.

---

## 🚀 Features

- Create new orders.
- Retrieve list of orders.
- Update and manage order status.
- Service registration/discovery via **Eureka**.
- Dynamic configuration with **Spring Cloud Config Server**.

---

## ⚙️ Configuration

### 1. Using H2 DB

```
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:orderdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 2. Using Mysql

```
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://database:3306/orderdb
    username: <YOUR_USERNAME>
    password: <YOUR_PASSWORD>
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

## 📂 Code Structure

```
order-service/
│── pom.xml                        # Maven build file
│── Dockerfile                     # Docker build file
│── README.md                      # Project documentation
│
│── src/
│   ├── main/java/com/example/orderservice/
│   │   ├── controller/            # REST controllers (OrderController, etc.)
│   │   ├── dto/                   # Data Transfer Objects
│   │   │   ├── request/           # Incoming API requests
│   │   │   ├── response/          # API responses
│   │   │   └── internal/          # Inter-service messages/events
│   │   ├── entity/                # JPA entities (Order, etc.)
│   │   ├── exception/             # Custom exceptions & handlers
│   │   ├── repository/            # Spring Data JPA repositories
│   │   ├── service/               # Business logic
│   │   ├── config/                # Configuration (Eureka, Swagger, etc.)
│   │   └── OrderServiceApplication.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── static/                # Static resources
│       └── templates/             # Thymeleaf templates (if used)
│
└── test/java/com/example/orderservice/
    ├── controller/                # Controller tests
    ├── service/                   # Service layer tests
    └── repository/                # Repository tests
```
