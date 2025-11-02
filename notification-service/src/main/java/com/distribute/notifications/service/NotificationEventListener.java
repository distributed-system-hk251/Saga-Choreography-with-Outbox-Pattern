package com.distribute.notifications.service;

import com.distribute.notifications.dto.NotificationDto;
import com.distribute.notifications.dto.OrderEventDto;
import com.distribute.notifications.dto.PaymentEventDto;
import com.distribute.notifications.entity.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    @Autowired
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Listen to Order events from Debezium CDC
     * Topic: outbox.event.Order
     */
    @KafkaListener(topics = "outbox.event.Order", groupId = "notification-service-group")
    public void onOrderEvent(String message) {
        log.info("Received order event from Debezium CDC");
        
        try {
            // Parse Debezium Outbox Router format
            JsonNode rootNode = objectMapper.readTree(message);
            
            // Payload is a JSON string, not an object
            String payloadString = rootNode.path("payload").asText();
            OrderEventDto orderEvent = objectMapper.readValue(payloadString, OrderEventDto.class);
            
            log.info("Processing order event - Type: {}, OrderId: {}, Status: {}", 
                    orderEvent.getEventType(), orderEvent.getOrderId(), orderEvent.getStatus());

            // Create notification based on event type
            NotificationDto notification = NotificationDto.builder()
                    .orderId(orderEvent.getOrderId())
                    .type(determineNotificationType(orderEvent.getEventType()))
                    .message(createOrderNotificationMessage(orderEvent.getEventType(), 
                            orderEvent.getOrderId(), orderEvent.getStatus()))
                    .build();

            notificationService.createNotification(notification);
            log.info("Successfully created notification for order event: {}", orderEvent.getEventType());

        } catch (Exception e) {
            log.error("Error processing order event: {}", message, e);
        }
    }

    /**
     * Listen to Payment events from Debezium CDC
     * Topic: dbserver1.payment_db.payments (example - adjust based on your Debezium config)
     */
    @KafkaListener(topics = "${kafka.topics.payment-cdc:outbox.event.Payment}", groupId = "notification-service-group")
    public void onPaymentEvent(String message) {
        log.info("Received payment event from Debezium CDC");
        
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode payload = rootNode.path("payload");
            
            // For Debezium CDC, check operation type
            String operation = rootNode.path("op").asText(); // c=create, u=update, d=delete
            
            if ("c".equals(operation) || "u".equals(operation)) {
                JsonNode after = payload.path("after");
                
                Integer paymentId = after.path("id").asInt();
                Integer orderId = after.path("order_id").asInt();
                String status = after.path("status").asText();
                
                log.info("Processing payment event - Op: {}, PaymentId: {}, OrderId: {}, Status: {}", 
                        operation, paymentId, orderId, status);

                // Only send notification for significant status changes
                if (shouldNotifyPaymentStatus(status)) {
                    NotificationDto notification = NotificationDto.builder()
                            .orderId(orderId)
                            .type(determinePaymentNotificationType(status))
                            .message(createPaymentNotificationMessage(status, orderId, paymentId))
                            .build();

                    notificationService.createNotification(notification);
                    log.info("Successfully created notification for payment status: {}", status);
                }
            }

        } catch (Exception e) {
            log.error("Error processing payment event: {}", message, e);
        }
    }

    /**
     * Determine notification type based on order event type
     */
    private String determineNotificationType(String eventType) {
        return switch (eventType) {
            case "ORDER_CREATED" -> NotificationType.ORDER_CONFIRMATION.name();
            case "ORDER_CONFIRMED" -> NotificationType.ORDER_CONFIRMATION.name();
            case "ORDER_CANCELLED" -> NotificationType.ORDER_CANCELLATION.name();
            case "ORDER_COMPLETED" -> NotificationType.ORDER_COMPLETION.name();
            case "ORDER_FAILED" -> NotificationType.ORDER_FAILURE.name();
            default -> NotificationType.GENERAL.name();
        };
    }

    /**
     * Determine notification type based on payment status
     */
    private String determinePaymentNotificationType(String status) {
        return switch (status) {
            case "PAID" -> NotificationType.PAYMENT_SUCCESS.name();
            case "FAILED" -> NotificationType.PAYMENT_FAILURE.name();
            case "REFUND" -> NotificationType.PAYMENT_REFUND.name();
            default -> NotificationType.GENERAL.name();
        };
    }

    /**
     * Check if payment status change requires notification
     */
    private boolean shouldNotifyPaymentStatus(String status) {
        return "PAID".equals(status) || "FAILED".equals(status) || "REFUND".equals(status);
    }

    /**
     * Create notification message for order events
     */
    private String createOrderNotificationMessage(String eventType, Integer orderId, String status) {
        return switch (eventType) {
            case "ORDER_CREATED" -> String.format("Order #%d has been created successfully", orderId);
            case "ORDER_CONFIRMED" -> String.format("Order #%d has been confirmed", orderId);
            case "ORDER_CANCELLED" -> String.format("Order #%d has been cancelled", orderId);
            case "ORDER_COMPLETED" -> String.format("Order #%d has been completed", orderId);
            case "ORDER_FAILED" -> String.format("Order #%d has failed", orderId);
            default -> String.format("Order #%d - Status: %s", orderId, status);
        };
    }

    /**
     * Create notification message for payment events
     */
    private String createPaymentNotificationMessage(String status, Integer orderId, Integer paymentId) {
        return switch (status) {
            case "PAID" -> String.format("Payment successful for order #%d", orderId);
            case "FAILED" -> String.format("Payment failed for order #%d. Please try again", orderId);
            case "REFUND" -> String.format("Refund processed for order #%d", orderId);
            default -> String.format("Payment #%d - Status: %s", paymentId, status);
        };
    }
}
