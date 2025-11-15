package com.app.order_service.service.impl;

import com.app.order_service.dto.request.CreateOrderForm;
import com.app.order_service.dto.request.Item;
import com.app.order_service.entity.Order;
import com.app.order_service.entity.OrderStatus;
import com.app.order_service.repository.OrderRepository;
import com.app.order_service.service.OrderService;
import com.app.order_service.service.OutboxService;
import com.app.order_service.service.utils.OrderMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public Order createOrder(CreateOrderForm form, String requestId) {
        try {
            Order order = OrderMapper.createOrderFormToOrder(form);

            BigDecimal totalAmount = calcTotalAmount(form.getItems());
            order.setTotalAmount(totalAmount);

            // Save order to database
            order = orderRepository.save(order);

            // ✅ Save event to outbox (Debezium will publish this to Kafka)
            outboxService.saveOrderCreatedEvent(order, requestId);

            return order;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Integer orderId, OrderStatus status, String failReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(status);
        order.setFailReason(failReason);

        // Save order to database
        order = orderRepository.save(order);

        // ✅ Save event to outbox (Debezium will publish this to Kafka)
        outboxService.saveOrderUpdatedEvent(order, "system");

        return order;
    }

    @Override
    @Transactional
    public Order handlePaymentFailed(Integer orderId, String failReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Update order status to PAYMENT_FAILED
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setFailReason(failReason);
        order = orderRepository.save(order);

        // ✅ Save ORDER_STATUS_UPDATED event to outbox
        outboxService.saveOrderUpdatedEvent(order, "system");

        // ✅ Save NOTIFICATION_SEND event to outbox
        String notificationMessage = String.format(
                "Payment failed for order #%d. Reason: %s",
                orderId,
                failReason != null ? failReason : "Unknown");
        outboxService.saveNotificationSendEvent(orderId, "PAYMENT_FAILED", notificationMessage, "system");

        // ✅ Save STOCK_RESERVE_RELEASE event to outbox
        outboxService.saveStockReserveReleaseEvent(order, "system");

        log.info("Payment failed for order {}: {}", orderId, failReason);
        log.info("✅ NOTIFICATION_SEND and STOCK_RESERVE_RELEASE events saved to outbox");

        return order;
    }

    @Override
    @Transactional
    public Order handlePaymentSuccess(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Update order status to PAID
        order.setStatus(OrderStatus.PAID);
        order = orderRepository.save(order);

        // ✅ Save ORDER_STATUS_UPDATED event to outbox
        outboxService.saveOrderUpdatedEvent(order, "system");

        // ✅ Save NOTIFICATION_SEND event to outbox
        String notificationMessage = String.format(
                "Payment successful for order #%d. Total amount: %s. Your order is being processed.",
                orderId,
                order.getTotalAmount());
        outboxService.saveNotificationSendEvent(orderId, "PAYMENT_SUCCESS", notificationMessage, "system");

        log.info("Payment successful for order {}", orderId);
        log.info("✅ NOTIFICATION_SEND event saved to outbox");

        return order;
    }

    @Override
    @Transactional
    public Order handleStockReserveFailed(Integer orderId, String failReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Update order status to STOCK_FAILED
        order.setStatus(OrderStatus.STOCK_FAILED);
        order.setFailReason(failReason);
        order = orderRepository.save(order);

        // ✅ Save ORDER_STATUS_UPDATED event to outbox
        outboxService.saveOrderUpdatedEvent(order, "system");

        // ✅ Save NOTIFICATION_SEND event to outbox
        String notificationMessage = String.format(
                "Order #%d cannot be processed. Reason: %s. Please try again later.",
                orderId,
                failReason != null ? failReason : "Stock not available");
        outboxService.saveNotificationSendEvent(orderId, "STOCK_RESERVE_FAILED", notificationMessage, "system");

        log.info("Stock reservation failed for order {}: {}", orderId, failReason);
        log.info("✅ NOTIFICATION_SEND event saved to outbox");

        return order;
    }

    @Override
    @Transactional
    public Order handlePaymentRefund(Integer orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Update order status to REFUNDED
        order.setStatus(OrderStatus.REFUNDED);
        order.setFailReason(reason);
        order = orderRepository.save(order);

        // ✅ Save ORDER_STATUS_UPDATED event to outbox
        outboxService.saveOrderUpdatedEvent(order, "system");

        // ✅ Save NOTIFICATION_SEND event to outbox
        String notificationMessage = String.format(
                "Order #%d has been refunded. Amount: %s. Reason: %s",
                orderId,
                order.getTotalAmount(),
                reason != null ? reason : "Customer request");
        outboxService.saveNotificationSendEvent(orderId, "PAYMENT_REFUND", notificationMessage, "system");

        // ✅ Save STOCK_RESERVE_RELEASE event to outbox
        outboxService.saveStockReserveReleaseEvent(order, "system");

        log.info("Payment refunded for order {}: {}", orderId, reason);
        log.info("✅ NOTIFICATION_SEND and STOCK_RESERVE_RELEASE events saved to outbox");

        return order;
    }

    public BigDecimal calcTotalAmount(List<Item> items) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", items);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://product-service:8084/api/products/total_amount")) // <-- adjust path
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());

            // Expecting: { "code": 200, "message": "...", "data": 5437.25 }
            int code = root.path("code").asInt(-1);
            if (code != 200) {
                String msg = root.path("message").asText("Unknown error");
                throw new RuntimeException("Remote calc failed: " + msg);
            }

            JsonNode data = root.path("data");

            BigDecimal total = data.isNumber() ? data.decimalValue() : new BigDecimal(data.asText());
            return total.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate total amount: " + e.getMessage(), e);
        }

    }

    @Override
    public List<Order> getOrdersByUserId(Integer userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        // remove items
        for (Order order : orders) {
            for (var item : order.getOrderItems()) {
                item.setOrderId(null);
            }
        }

        return orders;
    }

}
