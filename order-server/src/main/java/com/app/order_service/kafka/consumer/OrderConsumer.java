package com.app.order_service.kafka.consumer;

import com.app.order_service.entity.Order;
import com.app.order_service.entity.OrderStatus;
import com.app.order_service.kafka.producer.OrderProducer;
import com.app.order_service.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
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
     * Listen to Product CDC changes to know when stock is reserved/failed
     * Topic: dbserver1.productdb.products (or similar based on Debezium config)
     */
    @KafkaListener(
        topics = "${spring.kafka.topics.product-cdc:dbserver1.productdb.products}",
        groupId = "order-service-product-group"
    )
    public void onProductChange(String message) {
        try {
            log.info("Received product CDC event");
            
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode payload = rootNode.path("payload");
            String operation = payload.path("op").asText();
            
            // Only process UPDATE operations
            if (!"u".equals(operation)) {
                return;
            }
            
            JsonNode after = payload.path("after");
            
            // Extract product data to determine stock changes
            Integer productId = after.path("id").asInt();
            Integer stock = after.path("stock").asInt();
            
            log.debug("Product {} stock updated to: {}", productId, stock);
            
            // Note: In real implementation, you might need to track stock reservations
            // and correlate them with orders using a separate reservation table
            
        } catch (Exception e) {
            log.error("Error processing product CDC event", e);
        }
    }

    /**
     * Listen to Payment CDC changes to update order status
     * Topic: dbserver2.paymentdb.payments (or similar based on Debezium config)
     */
    @KafkaListener(
        topics = "${spring.kafka.topics.payment-cdc:dbserver2.paymentdb.payments}",
        groupId = "order-service-payment-group"
    )
    public void onPaymentChange(String message) {
        try {
            log.info("Received payment CDC event");
            
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode payload = rootNode.path("payload");
            String operation = payload.path("op").asText();
            
            // Process CREATE and UPDATE operations
            if (!"c".equals(operation) && !"u".equals(operation)) {
                return;
            }
            
            JsonNode after = payload.path("after");
            
            Integer paymentId = after.path("id").asInt();
            Integer orderId = after.path("order_id").asInt();
            String status = after.path("status").asText();
            BigDecimal amount = new BigDecimal(after.path("amount").asText());
            
            log.info("Payment {} for order {} changed to status: {}", paymentId, orderId, status);
            
            // Update order status based on payment status
            switch (status) {
                case "PAID":
                    // Payment successful
                    if ("u".equals(operation)) {
                        JsonNode before = payload.path("before");
                        String oldStatus = before.path("status").asText();
                        
                        if (!"PAID".equals(oldStatus)) {
                            orderService.updateOrderStatus(orderId, OrderStatus.PAID, null);
                            log.info("Order {} marked as PAID", orderId);
                        }
                    }
                    break;
                    
                case "FAILED":
                    // Payment failed
                    if ("u".equals(operation)) {
                        JsonNode before = payload.path("before");
                        String oldStatus = before.path("status").asText();
                        
                        if (!"FAILED".equals(oldStatus)) {
                            orderService.updateOrderStatus(orderId, OrderStatus.PAYMENT_FAILED, "Payment declined");
                            log.info("Order {} marked as PAYMENT_FAILED", orderId);
                            // CDC will publish order status change, Product service will release stock
                        }
                    }
                    break;
                    
                case "REFUND":
                    // Payment refunded
                    if ("u".equals(operation)) {
                        JsonNode before = payload.path("before");
                        String oldStatus = before.path("status").asText();
                        
                        if (!"REFUND".equals(oldStatus)) {
                            orderService.updateOrderStatus(orderId, OrderStatus.REFUNDED, "Payment refunded");
                            log.info("Order {} marked as REFUNDED", orderId);
                            // CDC will publish order status change, Product service will release stock
                        }
                    }
                    break;
                    
                case "PENDING":
                    // Payment created, trigger payment authorization
                    if ("c".equals(operation)) {
                        orderProducer.publishPaymentAuthorize(orderId, amount);
                        log.info("Payment authorization triggered for order {}", orderId);
                    }
                    break;
                    
                default:
                    log.debug("Unhandled payment status: {}", status);
            }
            
        } catch (Exception e) {
            log.error("Error processing payment CDC event", e);
        }
    }

}
