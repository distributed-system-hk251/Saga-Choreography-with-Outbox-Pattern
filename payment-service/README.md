# Payment Service

A comprehensive Spring Boot microservice for handling payment operations in a distributed system.

## Overview

The Payment Service provides REST APIs for managing payments, including creating payments, processing transactions, updating payment status, and handling refunds. It integrates with Kafka for event publishing and supports various payment methods.

## Architecture

### Entities
- **Payment**: Core payment entity with order mapping, amount, method, status, and timestamps
- **PaymentMethod**: Enum for different payment methods (CARD_PAYMENT, CASH, BANK_TRANSFER, DIGITAL_WALLET, CRYPTO)
- **PaymentStatus**: Enum for payment states (PAID, REFUND, PENDING, FAILED, CANCELLED, PARTIALLY_REFUNDED)

### Layers
- **Controller**: REST API endpoints for payment operations
- **Service**: Business logic for payment processing and validation
- **Repository**: Data access layer with Spring Data JPA
- **DTOs**: Request/Response data transfer objects with validation
- **Exception Handling**: Global exception handler with custom exceptions

## Features

### Core Operations
- ✅ Create new payments
- ✅ Retrieve payment by ID
- ✅ Get payments by order ID
- ✅ Update payment status
- ✅ Process payments (simulate payment gateway)
- ✅ Handle refunds
- ✅ Payment statistics and reporting

### Advanced Features
- ✅ Pagination and sorting for payment lists
- ✅ Payment filtering by status, method, amount range
- ✅ Kafka event publishing for payment events
- ✅ Comprehensive validation with custom error responses
- ✅ Eureka service discovery integration
- ✅ MySQL database integration with JPA

## API Endpoints

### Payment Management
```
POST   /api/v1/payments                    - Create a new payment
GET    /api/v1/payments/{id}              - Get payment by ID
GET    /api/v1/payments/order/{orderId}   - Get payments by order ID
GET    /api/v1/payments                   - Get all payments (paginated)
PUT    /api/v1/payments/{id}/status       - Update payment status
```

### Payment Operations
```
POST   /api/v1/payments/{id}/process      - Process a pending payment
POST   /api/v1/payments/{id}/refund       - Refund a paid payment
```

### Statistics & Utilities
```
GET    /api/v1/payments/statistics/total-amount    - Get total amount by status
GET    /api/v1/payments/statistics/by-method       - Get payment statistics by method
GET    /api/v1/payments/order/{orderId}/exists     - Check if payment exists for order
```

## Database Schema

The service uses the following table structure:

```sql
CREATE TABLE payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Configuration

### Application Properties
- **Database**: MySQL with JPA/Hibernate
- **Kafka**: Event publishing for payment status changes
- **Eureka**: Service discovery integration
- **Server Port**: 8083

### Required Environment
- Java 21+
- MySQL 8.0+
- Apache Kafka
- Eureka Discovery Server

## Usage Examples

### Create Payment
```bash
curl -X POST http://localhost:8083/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 12345,
    "amount": 99.99,
    "method": "CARD_PAYMENT",
    "description": "Order payment for electronics"
  }'
```

### Get Payment
```bash
curl http://localhost:8083/api/v1/payments/1
```

### Process Payment
```bash
curl -X POST http://localhost:8083/api/v1/payments/1/process
```

### Update Status
```bash
curl -X PUT http://localhost:8083/api/v1/payments/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PAID",
    "reason": "Payment completed successfully"
  }'
```

## Event Publishing

The service publishes events to Kafka topic `payment-events` for:
- Payment creation (`PAYMENT_CREATED`)
- Status updates (`PAYMENT_STATUS_UPDATED`)
- Payment processing (`PAYMENT_PROCESSED`)
- Refund processing (`PAYMENT_REFUNDED`)

Event format:
```json
{
  "eventType": "PAYMENT_CREATED",
  "paymentId": 1,
  "orderId": 12345,
  "status": "PENDING",
  "amount": 99.99
}
```

## Error Handling

The service provides comprehensive error handling with structured responses:

```json
{
  "timestamp": "2025-10-07T00:20:30",
  "status": 404,
  "error": "Payment Not Found",
  "message": "Payment not found with ID: 999"
}
```

For validation errors, additional field-level details are provided:
```json
{
  "timestamp": "2025-10-07T00:20:30",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request data",
  "validationErrors": {
    "amount": "Amount must be greater than 0",
    "orderId": "Order ID is required"
  }
}
```

## Development

### Running the DBMS
```bash
# Using Docker compose
docker-compose -f docker\docker-compose.yaml up -d
```

### Running the Service
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/payment-0.0.1-SNAPSHOT.jar
```

### Testing
```bash
# Run all tests
./mvnw test

# Run with specific profile
./mvnw test -Dspring.profiles.active=test
```

## Dependencies

Key dependencies used:
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Kafka
- Spring Cloud Netflix Eureka Client
- MySQL Connector
- Lombok
- Jakarta Validation
- H2 Database (for testing)

## Future Enhancements

Potential areas for expansion:
- Integration with real payment gateways (Stripe, PayPal, etc.)
- Payment retry mechanisms
- Webhook handling for payment gateway callbacks
- Advanced fraud detection
- Payment method tokenization
- Multi-currency support
- Payment analytics dashboard