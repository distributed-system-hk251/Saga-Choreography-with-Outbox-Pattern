package com.distribute.products.kafka.event;

public record StockReserveFailedEvent(Integer orderId, String reason) {}
