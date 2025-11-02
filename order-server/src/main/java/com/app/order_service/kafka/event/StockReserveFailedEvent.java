package com.app.order_service.kafka.event;

public record StockReserveFailedEvent(Integer orderId, String reason) {}
