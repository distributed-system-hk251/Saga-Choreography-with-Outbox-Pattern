package com.distribute.payment.kafka.event;

public record PaymentAuthorizeSucceededEvent(Integer orderId, Integer paymentId) {
}
