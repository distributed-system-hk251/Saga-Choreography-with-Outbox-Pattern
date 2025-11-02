# Notification Service

A Spring Boot microservice for handling notifications in a distributed system.

## Features

- **RESTful API** for notification management
- **Kafka Integration** for event-driven notifications (connects to external Kafka service)
- **MySQL Database** for persistent storage
- **Eureka Discovery** for service registration
- **Docker Support** with multi-stage builds
- **Comprehensive Testing** with JUnit 5
- **Exception Handling** with global error handlers
- **Health Checks** and monitoring endpoints

## Architecture

### Database Structure

```sql
CREATE TABLE notifications (
    id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
```

### Entities

- `Notification`: Core entity with fields for order tracking, message content, and timestamps

### API Endpoints

- `GET /api/v1/notifications` - Get all notifications with pagination
- `GET /api/v1/notifications/{id}` - Get notification by ID
- `GET /api/v1/notifications/order/{orderId}` - Get notifications by order ID
- `GET /api/v1/notifications/type/{type}` - Get notifications by type
- `POST /api/v1/notifications/create` - Create new notification
- `PUT /api/v1/notifications/{id}/update` - Update notification
- `DELETE /api/v1/notifications/{id}/delete` - Delete notification
- `DELETE /api/v1/notifications/order/{orderId}/delete` - Delete notifications by order ID
- `GET /api/v1/notifications/health` - Health check

### Notification Types (String values)

- `info` - Information notifications
- `alert` - Alert notifications
- `warning` - Warning notifications
- `error` - Error notifications

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0**
- **Spring Data JPA**
- **Spring Kafka**
- **MySQL 8.0**
- **Docker & Docker Compose**
- **Maven**
- **Lombok**

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- MySQL Database (local or Docker)
- External Kafka service (provided by other microservice)

### Running Standalone (Recommended)

This service is designed to connect to external Kafka and MySQL services running in your distributed system.

1. **Ensure external services are available:**

   - MySQL Database on `localhost:3309`
   - Kafka Broker on `localhost:9092` (managed by other services)

2. **Run the notification service:**

```bash
mvn spring-boot:run
```

The service will start on port `8082` and connect to external dependencies.

### Running with Docker (Development Only)

If you need to run with all dependencies for development:

```bash
cd docker
docker-compose up -d mysql  # Only start MySQL, Kafka managed externally
```

### Building the Application

```bash
# Build JAR
mvn clean package

# Build Docker image
docker build -f docker/Dockerfile -t notification-service .

# Run tests
mvn test
```

## Configuration

### Production Setup

- **Database**: Connects to external MySQL
- **Kafka**: Connects to external Kafka cluster (managed by message service)
- **Service Discovery**: Registers with Eureka server

### Environment Variables

| Variable                         | Description       | Default                                      |
| -------------------------------- | ----------------- | -------------------------------------------- |
| `SPRING_DATASOURCE_URL`          | Database URL      | `jdbc:mysql://localhost:3309/notificationdb` |
| `SPRING_DATASOURCE_USERNAME`     | Database username | `root`                                       |
| `SPRING_DATASOURCE_PASSWORD`     | Database password | `root`                                       |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers     | `localhost:9092`                             |

### Profiles

- `default`: Local development
- `docker`: Docker container deployment
- `test`: Testing environment

## API Documentation

### Create Notification

```http
POST /api/v1/notifications
Content-Type: application/json

{
  "orderId": 12345,
  "type": "ORDER_CREATED",
  "message": "Your order has been created successfully",
  "recipient": "user@example.com"
}
```

### Get Notifications with Pagination

```http
GET /api/v1/notifications?page=0&size=10&sortBy=createdAt&sortDir=desc
```

Response:

```json
{
  "notifications": [...],
  "currentPage": 0,
  "totalItems": 100,
  "totalPages": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

## Kafka Integration

The service consumes events from Kafka topics:

- `NOTIFICATION_SEND`: Direct notification events
- `order-events`: Order-related events that trigger notifications

## Database Schema

```sql
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    recipient VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Monitoring

### Health Checks

- Application: `GET /api/v1/notifications/health`
- Spring Actuator: `GET /actuator/health`

### Metrics

Available at `/actuator/metrics`

## Development

### Code Structure

```
src/main/java/com/distribute/notifications/
├── NotificationsApplication.java
├── config/
│   └── KafkaConfig.java
├── controller/
│   └── NotificationController.java
├── dto/
│   └── NotificationDto.java
├── entity/
│   └── Notification.java
├── exception/
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── NotificationNotFoundException.java
├── kafka/
│   └── NotificationEventConsumer.java
├── repository/
│   └── NotificationRepository.java
└── service/
    └── NotificationService.java
```

### Testing

Run tests with:

```bash
mvn test
```

The test suite includes:

- Unit tests for service layer
- Integration tests with TestRestTemplate
- Repository tests with H2 in-memory database

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
