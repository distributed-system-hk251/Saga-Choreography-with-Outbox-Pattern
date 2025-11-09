package com.distribute.payment.repository;

import com.distribute.payment.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    
    /**
     * Find all outbox events
     */
    List<Outbox> findAll();
    
    /**
     * Find outbox events by aggregate type
     */
    List<Outbox> findByAggregateType(String aggregateType);
    
    /**
     * Find outbox events by aggregate id
     */
    List<Outbox> findByAggregateId(String aggregateId);
    
    /**
     * Find outbox events by event type
     */
    List<Outbox> findByEventType(String eventType);
}
