package com.app.order_service.service.impl;

import com.app.order_service.entity.Order;
import com.app.order_service.entity.Outbox;
import com.app.order_service.repository.OutboxRepository;
import com.app.order_service.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements OutboxService {
    
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveOrderCreatedEvent(Order order, String requestId) {
        try {
            String payload = buildOrderPayload(order, requestId);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Order")
                    .aggregateId(order.getId().toString())
                    .eventType("ORDER_CREATED")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved OrderCreated event to outbox for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to save OrderCreated event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveOrderUpdatedEvent(Order order, String requestId) {
        try {
            String payload = buildOrderPayload(order, requestId);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Order")
                    .aggregateId(order.getId().toString())
                    .eventType("ORDER_STATUS_UPDATED")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved OrderUpdated event to outbox for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to save OrderUpdated event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveNotificationSendEvent(Integer orderId, String type, String message, String requestId) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("orderId", orderId);
            payloadMap.put("type", type);
            payloadMap.put("message", message);
            payloadMap.put("requestId", requestId);
            
            String payload = objectMapper.writeValueAsString(payloadMap);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Order")
                    .aggregateId(orderId.toString())
                    .eventType("NOTIFICATION_SEND")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved NotificationSend event to outbox for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to save NotificationSend event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveStockReserveReleaseEvent(Order order, String requestId) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("orderId", order.getId());
            payloadMap.put("requestId", requestId);
            
            // Convert OrderItems to simple list format
            List<Map<String, Integer>> items = order.getOrderItems().stream()
                    .map(item -> {
                        Map<String, Integer> itemMap = new HashMap<>();
                        itemMap.put("productId", item.getProductId());
                        itemMap.put("quantity", item.getQuantity());
                        return itemMap;
                    })
                    .collect(Collectors.toList());
            
            payloadMap.put("items", items);
            
            String payload = objectMapper.writeValueAsString(payloadMap);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Order")
                    .aggregateId(order.getId().toString())
                    .eventType("STOCK_RESERVE_RELEASE")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved StockReserveRelease event to outbox for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to save StockReserveRelease event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    private String buildOrderPayload(Order order, String requestId) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("orderId", order.getId());
            payloadMap.put("userId", order.getUserId());
            payloadMap.put("status", order.getStatus().name());
            payloadMap.put("totalAmount", order.getTotalAmount());
            payloadMap.put("failReason", order.getFailReason());
            payloadMap.put("requestId", requestId);
            
            // Convert OrderItems to simple list format
            List<Map<String, Integer>> items = order.getOrderItems().stream()
                    .map(item -> {
                        Map<String, Integer> itemMap = new HashMap<>();
                        itemMap.put("productId", item.getProductId());
                        itemMap.put("quantity", item.getQuantity());
                        return itemMap;
                    })
                    .collect(Collectors.toList());
            
            payloadMap.put("items", items);
            
            return objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("Failed to build order payload: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize order payload", e);
        }
    }
}
