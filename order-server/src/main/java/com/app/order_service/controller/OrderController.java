package com.app.order_service.controller;

import com.app.order_service.dto.request.CreateOrderForm;
import com.app.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import com.app.order_service.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class.getName());

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<String>> createOrderChoreography(
            @RequestBody CreateOrderForm createOrderForm) {
        ZonedDateTime nowUtc7 = ZonedDateTime.now(ZoneId.of("UTC+7"));
        String requestId = "REQ-" + nowUtc7.toInstant().toEpochMilli();
        logger.info("Received Request with ID: {}, at time: {}", requestId, nowUtc7);
        orderService.createOrder(createOrderForm, requestId);

        ApiResponse<String> response = new ApiResponse<>(200, "Order created successfully", "");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/users/{userId}")
    public ResponseEntity<ApiResponse<?>> getOrdersByUserId(@PathVariable Integer userId) {
        var orders = orderService.getOrdersByUserId(userId);
        
        ApiResponse<?> response = new ApiResponse<>(200, "Orders retrieved successfully", orders);
        return ResponseEntity.ok(response);
    }

}
