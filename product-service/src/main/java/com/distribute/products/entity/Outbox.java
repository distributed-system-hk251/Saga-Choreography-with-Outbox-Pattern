package com.distribute.products.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outbox {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "aggregate_type", nullable = false, length = 255)
    private String aggregateType;
    
    @Column(name = "aggregate_id", nullable = false, length = 255)
    private String aggregateId;
    
    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;
    
    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
