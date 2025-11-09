package com.distribute.notifications.service;

import com.distribute.notifications.dto.NotificationDto;
import com.distribute.notifications.entity.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
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
     * Only processes events with eventType = NOTIFICATION_SEND
     */
    @KafkaListener(topics = "outbox.event.Order", groupId = "notification-service-group")
    public void onOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "eventType", required = false) String eventType) {
        
        log.info("Received order event from topic: {}, partition: {}, offset: {}, eventType: {}", 
                topic, partition, offset, eventType);
        log.debug("Message content: {}", message);
        
        try {
            // ✅ Only process NOTIFICATION_SEND events (from Kafka header)
            if (!"NOTIFICATION_SEND".equals(eventType)) {
                log.debug("Skipping event - eventType: {} (not NOTIFICATION_SEND)", eventType);
                return;
            }
            
            log.info("Processing NOTIFICATION_SEND event");
            
            // Parse the payload (already transformed by Debezium Outbox Router)
            // Message has schema + payload structure, extract payload
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode payloadNode;
            
            if (rootNode.has("payload") && rootNode.has("schema")) {
                // Payload is expanded as JSON object
                payloadNode = rootNode.get("payload");
            } else {
                // Fallback: parse directly
                payloadNode = rootNode;
            }
            
            // Extract notification data from payload
            Integer orderId = payloadNode.path("orderId").asInt();
            String notificationType = payloadNode.path("type").asText();
            String notificationMessage = payloadNode.path("message").asText();
            
            log.info("Processing notification - OrderId: {}, Type: {}, Message: {}", 
                    orderId, notificationType, notificationMessage);

            // Create notification
            NotificationDto notification = NotificationDto.builder()
                    .orderId(orderId)
                    .type(mapNotificationType(notificationType))
                    .message(notificationMessage)
                    .build();

            notificationService.createNotification(notification);
            log.info("✅ Successfully created notification for order: {}", orderId);

        } catch (Exception e) {
            log.error("Error processing order event: {}", message, e);
        }
    }

    /**
     * Map notification type from payload to NotificationType enum
     */
    private String mapNotificationType(String type) {
        return switch (type) {
            case "ORDER_CREATED" -> NotificationType.ORDER_CONFIRMATION.name();
            case "ORDER_CONFIRMED" -> NotificationType.ORDER_CONFIRMATION.name();
            case "ORDER_CANCELLED" -> NotificationType.ORDER_CANCELLATION.name();
            case "ORDER_COMPLETED" -> NotificationType.ORDER_COMPLETION.name();
            case "ORDER_FAILED" -> NotificationType.ORDER_FAILURE.name();
            case "PAYMENT_FAILED" -> NotificationType.PAYMENT_FAILURE.name();
            case "PAYMENT_SUCCEEDED" -> NotificationType.PAYMENT_SUCCESS.name();
            default -> NotificationType.GENERAL.name();
        };
    }




}
