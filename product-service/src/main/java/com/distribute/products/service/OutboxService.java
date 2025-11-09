package com.distribute.products.service;

import com.distribute.products.kafka.event.Item;

import java.util.List;

public interface OutboxService {
    
    /**
     * Save stock updated event to outbox
     */
    void saveStockUpdatedEvent(Integer orderId, List<Item> items, String eventType, String requestId);
    
    /**
     * Save stock released event to outbox
     */
    void saveStockReleasedEvent(Integer orderId, List<Item> items, String requestId);
}
