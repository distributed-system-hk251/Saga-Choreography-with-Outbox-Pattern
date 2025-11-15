package com.app.order_service.kafka.event;

public record PaymentUnsuccessfulEvent(Integer orderId, Integer paymentId, String reason) {
}
