package com.app.order_service.service;

import java.util.List;

import com.app.order_service.dto.request.CreateOrderForm;
import com.app.order_service.entity.Order;
import com.app.order_service.entity.OrderStatus;

public interface OrderService {
    Order createOrder(CreateOrderForm form, String requestId);

    Order updateOrderStatus(Integer orderId, OrderStatus status, String failReason);

    List<Order> getOrdersByUserId(Integer userId);
    
    /**
     * Handle payment failure - update status, send notification, and release stock
     */
    Order handlePaymentFailed(Integer orderId, String failReason);
    
    /**
     * Handle payment success - update status and send notification
     */
    Order handlePaymentSuccess(Integer orderId);
    
    /**
     * Handle stock reservation failure - update status and send notification
     */
    Order handleStockReserveFailed(Integer orderId, String failReason);
    
    /**
     * Handle payment refund - update status, send notification, and release stock
     */
    Order handlePaymentRefund(Integer orderId, String reason);
}
