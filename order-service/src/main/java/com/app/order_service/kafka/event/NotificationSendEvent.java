package com.app.order_service.kafka.event;

public record NotificationSendEvent(
        Integer orderId,
        String type,
        String message) {
}
