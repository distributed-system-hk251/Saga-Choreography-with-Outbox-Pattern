package com.app.order_service.service;

import java.util.List;

import com.app.order_service.dto.request.CreateOrderForm;
import com.app.order_service.entity.Order;
import com.app.order_service.entity.OrderStatus;

public interface OrderService {
    Order createOrder(CreateOrderForm form, String requestId);

    Order updateOrderStatus(Integer orderId, OrderStatus status, String failReason);

    List<Order> getOrdersByUserId(Integer userId);
}
