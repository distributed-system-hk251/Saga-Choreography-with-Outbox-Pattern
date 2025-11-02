package com.distribute.payment.kafka.event;

public record PaymentAuthorizeFailedEvent(Integer orderId, Integer paymentId, String reason) {
}
