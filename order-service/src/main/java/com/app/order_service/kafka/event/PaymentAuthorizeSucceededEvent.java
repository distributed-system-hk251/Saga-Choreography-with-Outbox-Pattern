package com.app.order_service.kafka.event;

public record PaymentAuthorizeSucceededEvent(Integer orderId, Integer paymentId) {
}
