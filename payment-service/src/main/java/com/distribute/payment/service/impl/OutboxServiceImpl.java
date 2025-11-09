package com.distribute.payment.service.impl;

import com.distribute.payment.entity.Outbox;
import com.distribute.payment.entity.Payment;
import com.distribute.payment.repository.OutboxRepository;
import com.distribute.payment.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements OutboxService {
    
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void savePaymentProcessedEvent(Payment payment, String requestId) {
        try {
            String payload = buildPaymentPayload(payment, requestId, null);
            
            String eventType = payment.getStatus().name().equals("PAID") 
                ? "PAYMENT_AUTHORIZED" 
                : "PAYMENT_FAILED";
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Payment")
                    .aggregateId(payment.getOrderId().toString())
                    .eventType(eventType)
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved {} event to outbox for payment ID: {}, order ID: {}", 
                    eventType, payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to save payment processed event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void savePaymentRefundedEvent(Payment payment, String requestId, String reason) {
        try {
            String payload = buildPaymentPayload(payment, requestId, reason);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Payment")
                    .aggregateId(payment.getOrderId().toString())
                    .eventType("PAYMENT_REFUNDED")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved PaymentRefunded event to outbox for payment ID: {}, order ID: {}", 
                    payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to save payment refunded event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void savePaymentFailedEvent(Payment payment, String requestId, String reason) {
        try {
            String payload = buildPaymentPayload(payment, requestId, reason);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Payment")
                    .aggregateId(payment.getOrderId().toString())
                    .eventType("PAYMENT_FAILED")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved PaymentFailed event to outbox for payment ID: {}, order ID: {}", 
                    payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to save payment failed event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void savePaymentAuthorizeSucceededEvent(Payment payment, String requestId) {
        try {
            String payload = buildPaymentPayload(payment, requestId, null);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Payment")
                    .aggregateId(payment.getOrderId().toString())
                    .eventType("PAYMENT_AUTHORIZE_SUCCEEDED")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved PAYMENT_AUTHORIZE_SUCCEEDED event to outbox for payment ID: {}, order ID: {}", 
                    payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to save payment authorize succeeded event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void savePaymentAuthorizeFailedEvent(Payment payment, String requestId, String reason) {
        try {
            String payload = buildPaymentPayload(payment, requestId, reason);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Payment")
                    .aggregateId(payment.getOrderId().toString())
                    .eventType("PAYMENT_AUTHORIZE_FAILED")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved PAYMENT_AUTHORIZE_FAILED event to outbox for payment ID: {}, order ID: {}", 
                    payment.getId(), payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to save payment authorize failed event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    private String buildPaymentPayload(Payment payment, String requestId, String reason) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("paymentId", payment.getId());
            payloadMap.put("orderId", payment.getOrderId());
            payloadMap.put("amount", payment.getAmount());
            payloadMap.put("method", payment.getMethod() != null ? payment.getMethod().name() : null);
            payloadMap.put("status", payment.getStatus().name());
            payloadMap.put("requestId", requestId);
            
            if (reason != null) {
                payloadMap.put("reason", reason);
            }
            
            return objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("Failed to build payment payload: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize payment payload", e);
        }
    }
}
