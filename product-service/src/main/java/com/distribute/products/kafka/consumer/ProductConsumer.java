package com.distribute.products.kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import com.distribute.products.kafka.event.CreateOrderEvent;
import com.distribute.products.kafka.event.StockReserveReleaseEvent;
import com.distribute.products.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductConsumer {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductService productService;

    /**
     * Listen to Order Outbox events from Debezium CDC
     * Topic: outbox.event.Order (Debezium outbox transformed topic)
     * Filter by event_type: ORDER_CREATED
     * Reserve stock and respond with STOCK_RESERVE_SUCCEEDED or STOCK_RESERVE_FAILED
     */
    @KafkaListener(
        topics = "${spring.kafka.topics.order-outbox:outbox.event.Order}", 
        groupId = "product-service-order-group"
    )
    public void onOrderOutboxEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "eventType", required = false) String eventType) {
        try {
            log.info("Received order event from topic: {}, partition: {}, offset: {}, eventType: {}", 
                    topic, partition, offset, eventType);
            log.debug("Message content: {}", message);
            
            // Only process ORDER_CREATED events
            if ("ORDER_CREATED".equals(eventType)) {
                // Parse event payload (already transformed by Debezium Outbox Router)
                // Message has schema + payload structure, extract payload
                JsonNode rootNode = objectMapper.readTree(message);
                CreateOrderEvent event;
                
                if (rootNode.has("payload") && rootNode.has("schema")) {
                    // Payload is expanded as JSON object
                    event = objectMapper.treeToValue(rootNode.get("payload"), CreateOrderEvent.class);
                } else {
                    // Fallback: parse directly
                    event = objectMapper.readValue(message, CreateOrderEvent.class);
                }
                
                log.info("Processing ORDER_CREATED: orderId={}, items count={}", 
                    event.getOrderId(), event.getItems().size());
                
                // Handle stock reservation
                handleStockReservation(event);
            } else {
                log.debug("Ignoring event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process order outbox event: {}", message, e);
        }
    }

    /**
     * Listen to Order Outbox events to handle stock release
     * Topic: outbox.event.Order (Debezium outbox transformed topic)
     * Filter by event_type: STOCK_RESERVE_RELEASE
     * This is triggered when payment fails or order is cancelled
     */
    @KafkaListener(
        topics = "${spring.kafka.topics.order-outbox:outbox.event.Order}", 
        groupId = "product-service-release-group"
    )
    public void onOrderStockReleaseEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "eventType", required = false) String eventType) {
        try {
            log.info("Received order event from topic: {}, partition: {}, offset: {}, eventType: {}", 
                    topic, partition, offset, eventType);
            log.debug("Message content: {}", message);
            
            // Only process STOCK_RESERVE_RELEASE events
            if ("STOCK_RESERVE_RELEASE".equals(eventType)) {
                // Parse event payload (already transformed by Debezium Outbox Router)
                // Message has schema + payload structure, extract payload
                JsonNode rootNode = objectMapper.readTree(message);
                StockReserveReleaseEvent event;
                
                if (rootNode.has("payload") && rootNode.has("schema")) {
                    // Payload is expanded as JSON object
                    event = objectMapper.treeToValue(rootNode.get("payload"), StockReserveReleaseEvent.class);
                } else {
                    // Fallback: parse directly
                    event = objectMapper.readValue(message, StockReserveReleaseEvent.class);
                }
                
                log.info("Received STOCK_RESERVE_RELEASE for Order ID: {}", event.orderId());
                
                // Release stock by adding back the quantities
                productService.releaseStocks(event.orderId(), event.items());
                
                log.info("✅ Stock released successfully for order: {}", event.orderId());
            } else {
                log.debug("Ignoring event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process order stock release event: {}", message, e);
        }
    }

    /**
     * Handle stock reservation for new orders (ORDER_CREATED event)
     * Uses Outbox pattern - NO direct Kafka producer calls
     * All events are saved to outbox table and published by Debezium
     */
    private void handleStockReservation(CreateOrderEvent event) {
        try {
            log.info("Reserving stock for order: {}", event.getOrderId());
            
            // Reserve stock (decrease quantities) and save STOCK_RESERVE_SUCCEEDED event to outbox
            // ProductService handles both operations in same transaction using Outbox pattern
            productService.updateStocks(event.getOrderId(), event.getItems());
            
            log.info("Stock reserved successfully for order: {}", event.getOrderId());
            log.info("✅ STOCK_RESERVE_SUCCEEDED event saved to outbox, Debezium will publish it");
            
        } catch (Exception e) {
            log.error("Failed to reserve stock for order: {}, reason: {}", event.getOrderId(), e.getMessage());
            
            // Save STOCK_RESERVE_FAILED event to outbox
            // Need to call ProductService to handle transaction properly
            productService.saveStockReserveFailed(event.getOrderId(), event.getItems(), e.getMessage());
            
            log.info("✅ STOCK_RESERVE_FAILED event saved to outbox, Debezium will publish it");
        }
    }

}
