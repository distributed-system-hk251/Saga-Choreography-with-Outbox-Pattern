package com.app.order_service.service.utils;

import com.app.order_service.dto.request.CreateOrderForm;
import com.app.order_service.entity.Order;
import com.app.order_service.entity.OrderStatus;

import java.time.LocalDateTime;

public class OrderMapper {
    public static Order createOrderFormToOrder(CreateOrderForm form) {
        Order order = Order.builder()
                .userId(form.getUserId())
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();

        for (var item: form.getItems()) {
            order.addOrderItem(item.getProductId(), item.getQuantity());
        }

        return order;
    }
}
