package com.distribute.payment.service;

import com.distribute.payment.entity.Payment;

public interface OutboxService {
    
    /**
     * Save payment processed event to outbox
     */
    void savePaymentProcessedEvent(Payment payment, String requestId);
    
    /**
     * Save payment refunded event to outbox
     */
    void savePaymentRefundedEvent(Payment payment, String requestId, String reason);
    
    /**
     * Save payment failed event to outbox
     */
    void savePaymentFailedEvent(Payment payment, String requestId, String reason);
    
    /**
     * Save payment authorize succeeded event to outbox
     */
    void savePaymentAuthorizeSucceededEvent(Payment payment, String requestId);
    
    /**
     * Save payment authorize failed event to outbox
     */
    void savePaymentAuthorizeFailedEvent(Payment payment, String requestId, String reason);
}
