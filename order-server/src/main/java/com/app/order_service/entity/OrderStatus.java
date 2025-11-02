package com.app.order_service.entity;

public enum OrderStatus {
    PENDING, // Just created, waiting for stock reservation
    STOCK_RESERVED, // Product service confirmed stock
    STOCK_FAILED, // Product reservation failed
    PAYMENT_PENDING, // Waiting for payment service
    PAID, // Payment successful
    PAYMENT_FAILED, // Payment declined
    COMPLETED, // Order successfully finished (after sending notification)
    CANCELED, // Order canceled (by user or system)
    REFUNDED, // Payment refunded
}
