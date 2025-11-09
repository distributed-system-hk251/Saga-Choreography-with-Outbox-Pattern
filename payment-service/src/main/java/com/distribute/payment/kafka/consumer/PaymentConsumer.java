package com.distribute.payment.kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.distribute.payment.entity.Payment;
import com.distribute.payment.entity.PaymentMethod;
import com.distribute.payment.entity.PaymentStatus;
import com.distribute.payment.repository.PaymentRepository;
import com.distribute.payment.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Random;

@Slf4j
@Component
public class PaymentConsumer {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    private final Random random = new Random();

    /**
     * Handle payment authorization for orders
     * Creates payment and simulates payment processing
     * Uses Outbox pattern - NO direct Kafka producer calls
     */
    private void handlePaymentAuthorization(Integer orderId, BigDecimal amount) {
        try {
            log.info("Creating payment for order: {} with amount: {}", orderId, amount);
            
            // Create payment entity with PENDING status
            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .method(PaymentMethod.CARD_PAYMENT) // Default method
                    .status(PaymentStatus.PENDING)
                    .build();
            
            // Save payment
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created with ID: {} for order: {}", savedPayment.getId(), orderId);
            
            // Simulate payment processing (randomly succeed or fail)
            // In production, this would call external payment gateway API
            boolean paymentSuccess = simulatePaymentProcessing(amount);
            
            if (paymentSuccess) {
                // Payment successful
                savedPayment.setStatus(PaymentStatus.PAID);
                
                log.info("Payment processed successfully for order: {}", orderId);
                
                // Save payment and PAYMENT_AUTHORIZE_SUCCEEDED event to outbox in same transaction
                // Debezium will publish this event
                paymentService.savePaymentAuthorizeSucceeded(savedPayment);
                log.info("✅ PAYMENT_AUTHORIZE_SUCCEEDED event saved to outbox");
                
            } else {
                // Payment failed
                savedPayment.setStatus(PaymentStatus.FAILED);
                
                log.warn("Payment processing failed for order: {}", orderId);
                
                // Save payment and PAYMENT_AUTHORIZE_FAILED event to outbox in same transaction
                // Debezium will publish this event
                paymentService.savePaymentAuthorizeFailed(savedPayment, "Payment declined by gateway");
                log.info("✅ PAYMENT_AUTHORIZE_FAILED event saved to outbox");
            }
            
        } catch (Exception e) {
            log.error("Failed to handle payment authorization for order: {}", orderId, e);
            
            // On exception, also save failure event
            try {
                Payment failedPayment = Payment.builder()
                        .orderId(orderId)
                        .amount(amount)
                        .status(PaymentStatus.FAILED)
                        .build();
                
                paymentService.savePaymentAuthorizeFailed(
                    failedPayment,
                    "Exception during payment processing: " + e.getMessage()
                );
                log.info("✅ PAYMENT_AUTHORIZE_FAILED event saved to outbox due to exception");
            } catch (Exception outboxError) {
                log.error("Failed to save PAYMENT_AUTHORIZE_FAILED to outbox: {}", outboxError.getMessage());
            }
        }
    }

    /**
     * Simulate payment processing
     * Returns true for success, false for failure
     * In production, this would integrate with external payment gateway
     * 
     * Current logic:
     * - 80% chance of success
     * - 20% chance of failure
     * - Amounts over 10000 always fail (simulate insufficient funds)
     */
    private boolean simulatePaymentProcessing(BigDecimal amount) {
        // Simulate processing delay
        try {
            Thread.sleep(100 + random.nextInt(400)); // 100-500ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Amounts over 10000 always fail
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            log.info("Payment amount {} exceeds limit, simulating failure", amount);
            return false;
        }
        
        // 80% success rate
        boolean success = random.nextInt(100) < 80;
        log.info("Payment simulation result: {}", success ? "SUCCESS" : "FAILED");
        
        return success;
    }

    /**
     * Listen to PAYMENT_AUTHORIZE topic (direct publish from order-service)
     * This is for auto-triggered payment after stock reservation
     */
    @KafkaListener(
        topics = "PAYMENT_AUTHORIZE",
        groupId = "payment-service-authorize-group"
    )
    public void onPaymentAuthorizeEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            log.info("Received PAYMENT_AUTHORIZE from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.debug("Message content: {}", message);
            
            // Parse the message
            JsonNode rootNode = objectMapper.readTree(message);
            Integer orderId = rootNode.path("orderId").asInt();
            BigDecimal amount = new BigDecimal(rootNode.path("amount").asText());
            
            log.info("Processing PAYMENT_AUTHORIZE: orderId={}, amount={}", orderId, amount);
            
            // Handle payment authorization
            handlePaymentAuthorization(orderId, amount);
            
        } catch (Exception e) {
            log.error("Failed to process PAYMENT_AUTHORIZE event: {}", message, e);
        }
    }
}
