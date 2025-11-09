package com.distribute.products.service.impl;

import com.distribute.products.entity.Outbox;
import com.distribute.products.kafka.event.Item;
import com.distribute.products.repository.OutboxRepository;
import com.distribute.products.service.OutboxService;
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
    public void saveStockUpdatedEvent(Integer orderId, List<Item> items, String eventType, String requestId) {
        try {
            String payload = buildStockPayload(orderId, items, requestId);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Product")
                    .aggregateId(orderId.toString())
                    .eventType(eventType)
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved {} event to outbox for order ID: {}", eventType, orderId);
        } catch (Exception e) {
            log.error("Failed to save {} event to outbox: {}", eventType, e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveStockReleasedEvent(Integer orderId, List<Item> items, String requestId) {
        try {
            String payload = buildStockPayload(orderId, items, requestId);
            
            Outbox outbox = Outbox.builder()
                    .aggregateType("Product")
                    .aggregateId(orderId.toString())
                    .eventType("STOCK_RELEASED")
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("Saved StockReleased event to outbox for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to save StockReleased event to outbox: {}", e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
    
    private String buildStockPayload(Integer orderId, List<Item> items, String requestId) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("orderId", orderId);
            payloadMap.put("requestId", requestId);
            
            // Convert items to simple list format
            List<Map<String, Integer>> itemList = items.stream()
                    .map(item -> {
                        Map<String, Integer> itemMap = new HashMap<>();
                        itemMap.put("productId", item.getProductId());
                        itemMap.put("quantity", item.getQuantity());
                        return itemMap;
                    })
                    .collect(Collectors.toList());
            
            payloadMap.put("items", itemList);
            
            return objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("Failed to build stock payload: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize stock payload", e);
        }
    }
}
