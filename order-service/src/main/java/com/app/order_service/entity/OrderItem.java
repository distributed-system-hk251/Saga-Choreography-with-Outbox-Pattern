package com.app.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(OrderItemId.class)
@Builder
public class OrderItem {
    @Id
    @Column(name = "product_id")
    private Integer productId;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order orderId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
