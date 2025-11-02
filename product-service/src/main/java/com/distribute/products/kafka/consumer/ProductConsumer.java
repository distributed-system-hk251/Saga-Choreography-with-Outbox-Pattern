package com.distribute.products.kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;

import com.distribute.products.dto.OrderStatusFlow;
import com.distribute.products.kafka.event.CreateOrderEvent;
import com.distribute.products.kafka.event.StockReserveReleaseEvent;
import com.distribute.products.kafka.producer.ProductProducer;
import com.distribute.products.kafka.topic.KafkaTopics;
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
    
    @Autowired
    private ProductProducer productProducer;

    /**
     * Listen to Order events from Debezium Outbox Router
     * Handles different order statuses:
     * - PENDING: Reserve stock (decrease quantity)
     * - PAYMENT_FAILED/CANCELED: Release stock (increase quantity)
     */
    @KafkaListener(
        topics = "outbox.event.Order", 
        groupId = "product-service-group", 
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderEvent(String message) {
        try {
            log.info("Received order event from outbox");
            
            // Parse Debezium Outbox Router format
            JsonNode rootNode = objectMapper.readTree(message);
            String payloadString = rootNode.get("payload").asText();
            CreateOrderEvent event = objectMapper.readValue(payloadString, CreateOrderEvent.class);
            
            log.info("Processing order event: orderId={}, status={}, items count={}", 
                event.getOrderId(), event.getStatus(), event.getItems().size());
            
            // Handle based on order status
            if (OrderStatusFlow.requiresStockReservation(event.getStatus())) {
                // PENDING status: reserve stock
                handleStockReservation(event);
                
            } else if (OrderStatusFlow.requiresStockRelease(event.getStatus())) {
                // PAYMENT_FAILED/CANCELED/REFUNDED status: release stock
                handleStockRelease(event);
                
            } else {
                log.debug("Order status {} does not require stock operation", event.getStatus());
            }

        } catch (Exception e) {
            log.error("Failed to process order event: {}", message, e);
        }
    }

    /**
     * Listen to stock release requests from Order Service
     * This is triggered when payment fails or order is cancelled
     */
    @KafkaListener(
        topics = KafkaTopics.STOCK_RESERVE_RELEASE, 
        groupId = "product-service-group", 
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onStockReserveRelease(String message) {
        try {
            StockReserveReleaseEvent event = objectMapper.readValue(message, StockReserveReleaseEvent.class);
            log.info("Received StockReserveReleaseEvent for Order ID: {}", event.orderId());

            // Release stock by adding back the quantities
            productService.releaseStocks(event.orderId(), event.items());
            
            log.info("Stock released successfully for order: {}", event.orderId());

        } catch (Exception e) {
            log.error("Failed to process stock release: {}", message, e);
        }
    }

    /**
     * Handle stock reservation for new orders (PENDING status)
     */
    private void handleStockReservation(CreateOrderEvent event) {
        try {
            log.info("Reserving stock for order: {}", event.getOrderId());
            
            // Reserve stock (decrease quantities)
            productService.updateStocks(event.getOrderId(), event.getItems());
            
            // Publish success event
            productProducer.publishStockReserveSucceeded(event.getOrderId(), event.getItems());
            log.info("Stock reserved successfully for order: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to reserve stock for order: {}", event.getOrderId(), e);
            
            // Publish failure event
            productProducer.publishStockReserveFailed(event.getOrderId(), e.getMessage());
        }
    }

    /**
     * Handle stock release for cancelled/failed orders
     * (PAYMENT_FAILED, CANCELED, REFUNDED status)
     */
    private void handleStockRelease(CreateOrderEvent event) {
        try {
            log.info("Releasing stock for cancelled/failed order: {} with status: {}", 
                event.getOrderId(), event.getStatus());
            
            // Release stock (add back quantities)
            productService.releaseStocks(event.getOrderId(), event.getItems());
            
            log.info("Stock released successfully for order: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to release stock for order: {}", event.getOrderId(), e);
        }
    }
}
