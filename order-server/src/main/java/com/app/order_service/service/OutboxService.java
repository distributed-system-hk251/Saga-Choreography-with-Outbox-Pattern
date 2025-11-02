package com.app.order_service.service;

import com.app.order_service.entity.Order;

public interface OutboxService {
    
    /**
     * Save order created event to outbox
     */
    void saveOrderCreatedEvent(Order order, String requestId);
    
    /**
     * Save order updated event to outbox
     */
    void saveOrderUpdatedEvent(Order order, String requestId);
}
