package com.distribute.payment.kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.distribute.payment.dto.OrderEventDto;
import com.distribute.payment.dto.PaymentRequestDto;
import com.distribute.payment.dto.PaymentResponseDto;
import com.distribute.payment.exception.PaymentProcessingException;
import com.distribute.payment.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderEventListener {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ORDER_TOPIC = "outbox.event.Order";
    private static final String GROUP = "payment-service-group";

    @KafkaListener(topics = ORDER_TOPIC, groupId = GROUP, containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "eventType", required = false) String eventType) {

        log.info("Received message from topic: {}, partition: {}, offset: {}, eventType: {}", 
                topic, partition, offset, eventType);
        log.debug("Message content: {}", message);

        try {
            // Parse the JSON message into OrderEventDto
            OrderEventDto orderEvent = parseOrderEvent(message);

            if (orderEvent == null) {
                log.warn("Failed to parse order event message: {}", message);
                return;
            }

            // Set eventType from Kafka header (Debezium Outbox pattern)
            if (eventType != null) {
                orderEvent.setEventType(eventType);
            }

            log.info("Parsed order event: eventType={}, orderId={}, amount={}, status={}",
                    orderEvent.getEventType(), orderEvent.getOrderId(),
                    orderEvent.getTotalAmount(), orderEvent.getStatus());

            // Process the order event if it's a payment-triggering event
            if (orderEvent.shouldCreatePayment()) {
                processOrderEventForPayment(orderEvent);
            } else {
                log.debug("Order event {} does not require payment processing", orderEvent.getEventType());
            }

        } catch (Exception e) {
            log.error("Failed to process order event message: {}", message, e);
            // In a production environment, you might want to send this to a dead letter
            // queue
            // or implement retry logic with exponential backoff
        }
    }

    private OrderEventDto parseOrderEvent(String message) {
        try {
            // Parse Debezium Outbox Router format
            // Message structure with expanded JSON: { "schema": {...}, "payload": {...actual event data...} }
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(message);
            
            // Check if message has "payload" field (Debezium Outbox format with schema)
            if (rootNode.has("payload") && rootNode.has("schema")) {
                // Payload is already expanded as JSON object (not string)
                com.fasterxml.jackson.databind.JsonNode payloadNode = rootNode.get("payload");
                return objectMapper.treeToValue(payloadNode, OrderEventDto.class);
            } else if (rootNode.has("payload")) {
                // Fallback: payload might be a string
                String payloadString = rootNode.get("payload").asText();
                return objectMapper.readValue(payloadString, OrderEventDto.class);
            } else {
                // Fallback: try to parse directly
                return objectMapper.readValue(message, OrderEventDto.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse order event JSON: {}", message, e);
            return null;
        }
    }

    private void processOrderEventForPayment(OrderEventDto orderEvent) {
        try {
            // Check if payment already exists for this order
            boolean paymentExists = paymentService.existsByOrderId(orderEvent.getOrderId());

            if (paymentExists) {
                log.info("Payment already exists for order: {}, skipping payment creation",
                        orderEvent.getOrderId());
                return;
            }

            // Create payment request from order event
            PaymentRequestDto paymentRequest = createPaymentRequest(orderEvent);

            // Create the payment
            PaymentResponseDto createdPayment = paymentService.createPayment(paymentRequest);

            log.info("Successfully created payment ID: {} for order: {} with amount: {}",
                    createdPayment.getId(), orderEvent.getOrderId(), orderEvent.getTotalAmount());

            // Optionally, you can trigger automatic payment processing here
            // if the business logic requires it
            if (shouldAutoProcessPayment(orderEvent)) {
                try {
                    // PaymentResponseDto processedPayment =
                    // paymentService.processPayment(createdPayment.getId());
                    // log.info("Auto-processed payment ID: {} with status: {}",
                    // processedPayment.getId(), processedPayment.getStatus());
                } catch (PaymentProcessingException e) {
                    log.warn("Auto-processing failed for payment ID: {}, reason: {}",
                            createdPayment.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Failed to create payment for order: {}", orderEvent.getOrderId(), e);
            throw new PaymentProcessingException("Failed to process order event for payment creation", e);
        }
    }

    private PaymentRequestDto createPaymentRequest(OrderEventDto orderEvent) {
        return PaymentRequestDto.builder()
                .orderId(orderEvent.getOrderId())
                .amount(orderEvent.getTotalAmount())
                .method(orderEvent.getPaymentMethodWithFallback())
                .description(String.format("Payment for order %d - %s",
                        orderEvent.getOrderId(),
                        orderEvent.getDescription() != null ? orderEvent.getDescription() : "Auto-generated"))
                .build();
    }

    private boolean shouldAutoProcessPayment(OrderEventDto orderEvent) {
        // Define business logic for when to auto-process payments
        // For example: only auto-process for confirmed orders or specific payment
        // methods
        return orderEvent.isOrderConfirmedEvent() &&
                (orderEvent.getPaymentMethodWithFallback() == com.distribute.payment.entity.PaymentMethod.DIGITAL_WALLET
                        ||
                        orderEvent
                                .getPaymentMethodWithFallback() == com.distribute.payment.entity.PaymentMethod.CARD_PAYMENT);
    }

    // Health check method to verify listener is working
    public String getListenerStatus() {
        return String.format("OrderEventListener is active, listening to topic: %s with consumer group: %s",
                ORDER_TOPIC, GROUP);
    }
}
