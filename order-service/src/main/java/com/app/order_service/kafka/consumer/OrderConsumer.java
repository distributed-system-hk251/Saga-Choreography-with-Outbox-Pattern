package com.app.order_service.kafka.consumer;

import com.app.order_service.entity.Order;
import com.app.order_service.entity.OrderStatus;
import com.app.order_service.kafka.producer.OrderProducer;
import com.app.order_service.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderConsumer {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderProducer orderProducer;

    /**
     * Listen to Product Outbox events via Debezium CDC
     * Topic: outbox.event.Product (Debezium outbox transformed topic)
     * Filter by event_type: STOCK_RESERVE_SUCCEEDED, STOCK_RESERVE_FAILED
     */
    @KafkaListener(
        topics = "${spring.kafka.topics.product-outbox:outbox.event.Product}",
        groupId = "order-service-product-group"
    )
    public void onProductOutboxEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "eventType", required = false) String eventType) {
        try {
            log.info("Received product event from topic: {}, partition: {}, offset: {}, eventType: {}", 
                    topic, partition, offset, eventType);
            log.debug("Message content: {}", message);
            
            // Parse event payload (already transformed by Debezium Outbox Router)
            // Message has schema + payload structure, extract payload
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode eventData;
            
            if (rootNode.has("payload") && rootNode.has("schema")) {
                // Payload is expanded as JSON object
                eventData = rootNode.get("payload");
            } else {
                // Fallback: parse directly
                eventData = rootNode;
            }
            
            Integer orderId = eventData.path("orderId").asInt();
            
            log.info("Processing product event type: {}", eventType);
            
            switch (eventType) {
                case "STOCK_RESERVE_SUCCEEDED":
                    // Stock reserved successfully, update order status
                    log.info("Stock reserved successfully for order {}", orderId);
                    Order updatedOrder = orderService.updateOrderStatus(orderId, OrderStatus.STOCK_RESERVED, null);
                    
                    // Automatically trigger payment authorization since we don't have /pay API yet
                    if (updatedOrder != null && updatedOrder.getTotalAmount() != null) {
                        orderProducer.publishPaymentAuthorize(orderId, updatedOrder.getTotalAmount());
                        log.info("Auto-triggered payment authorization for order {} with amount {}", 
                                orderId, updatedOrder.getTotalAmount());
                    }
                    break;
                    
                case "STOCK_RESERVE_FAILED":
                    // Stock reservation failed
                    String reason = eventData.path("reason").asText("Stock not available");
                    log.info("Stock reservation failed for order {}: {}", orderId, reason);
                    
                    // Handle stock reservation failure: update status and send notification
                    // All events (ORDER_STATUS_UPDATED, NOTIFICATION_SEND) 
                    // will be saved to outbox in one transaction
                    orderService.handleStockReserveFailed(orderId, reason);
                    log.info("✅ Stock reservation failure handled for order {}", orderId);
                    break;
                    
                default:
                    log.debug("Unhandled product event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing product outbox CDC event", e);
        }
    }

    /**
     * Listen to Payment Outbox events via Debezium CDC
     * Topic: outbox.event.Payment (Debezium outbox transformed topic)
     * Filter by event_type: PAYMENT_AUTHORIZE_SUCCEEDED, PAYMENT_AUTHORIZE_FAILED
     */
    @KafkaListener(
        topics = "${spring.kafka.topics.payment-outbox:outbox.event.Payment}",
        groupId = "order-service-payment-group"
    )
    public void onPaymentOutboxEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "eventType", required = false) String eventType) {
        try {
            log.info("Received payment event from topic: {}, partition: {}, offset: {}, eventType: {}", 
                    topic, partition, offset, eventType);
            log.debug("Message content: {}", message);
            
            // Parse event payload (already transformed by Debezium Outbox Router)
            // Message has schema + payload structure, extract payload
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode eventData;
            
            if (rootNode.has("payload") && rootNode.has("schema")) {
                // Payload is expanded as JSON object
                eventData = rootNode.get("payload");
            } else {
                // Fallback: parse directly
                eventData = rootNode;
            }
            
            Integer orderId = eventData.path("orderId").asInt();
            
            log.info("Processing payment event type: {}", eventType);
            
            switch (eventType) {
                case "PAYMENT_AUTHORIZE_SUCCEEDED":
                    // Payment authorized successfully
                    log.info("Payment authorized successfully for order {}", orderId);
                    
                    // Handle payment success: update status and send notification
                    // All events (ORDER_STATUS_UPDATED, NOTIFICATION_SEND) 
                    // will be saved to outbox in one transaction
                    orderService.handlePaymentSuccess(orderId);
                    log.info("✅ Payment success handled for order {}", orderId);
                    break;
                    
                case "PAYMENT_AUTHORIZE_FAILED":
                    // Payment authorization failed
                    String reason = eventData.path("reason").asText("Payment declined");
                    log.info("Payment authorization failed for order {}: {}", orderId, reason);
                    
                    // Handle payment failure: update status, send notification, and release stock
                    // All events (ORDER_STATUS_UPDATED, NOTIFICATION_SEND, STOCK_RESERVE_RELEASE) 
                    // will be saved to outbox in one transaction
                    orderService.handlePaymentFailed(orderId, reason);
                    log.info("✅ Payment failure handled for order {}", orderId);
                    break;
                    
                case "PAYMENT_REFUNDED":
                    // Payment refunded
                    String refundReason = eventData.path("reason").asText("Customer request");
                    log.info("Payment refunded for order {}: {}", orderId, refundReason);
                    
                    // Handle payment refund: update status, send notification, and release stock
                    // All events (ORDER_STATUS_UPDATED, NOTIFICATION_SEND, STOCK_RESERVE_RELEASE) 
                    // will be saved to outbox in one transaction
                    orderService.handlePaymentRefund(orderId, refundReason);
                    log.info("✅ Payment refund handled for order {}", orderId);
                    break;
                    
                default:
                    log.debug("Unhandled payment event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing payment outbox CDC event", e);
        }
    }

}
