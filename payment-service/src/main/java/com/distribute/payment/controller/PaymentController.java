package com.distribute.payment.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.distribute.payment.dto.PaymentProcessDto;
import com.distribute.payment.dto.PaymentRefundDto;
import com.distribute.payment.dto.PaymentRequestDto;
import com.distribute.payment.dto.PaymentResponseDto;
import com.distribute.payment.dto.PaymentStatusUpdateDto;
import com.distribute.payment.entity.PaymentStatus;
import com.distribute.payment.service.PaymentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        log.info("Received request to create payment for order: {}", requestDto.getOrderId());

        PaymentResponseDto response = paymentService.createPayment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(
            @PathVariable @Min(value = 1, message = "Payment ID must be positive") Integer id) {
        log.info("Received request to get payment with ID: {}", id);

        PaymentResponseDto response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByOrderId(
            @PathVariable @Min(value = 1, message = "Order ID must be positive") Integer orderId) {
        log.info("Received request to get payments for order: {}", orderId);

        List<PaymentResponseDto> response = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) PaymentStatus status) {

        log.info("Received request to get payments - page: {}, size: {}, sortBy: {}, sortDir: {}, status: {}",
                page, size, sortBy, sortDir, status);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentResponseDto> response;
        if (status != null) {
            response = paymentService.getPaymentsByStatus(status, pageable);
        } else {
            response = paymentService.getAllPayments(pageable);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDto> updatePaymentStatus(
            @PathVariable @Min(value = 1, message = "Payment ID must be positive") Integer id,
            @Valid @RequestBody PaymentStatusUpdateDto statusUpdateDto) {

        log.info("Received request to update payment status for ID: {} to {}", id, statusUpdateDto.getStatus());

        PaymentResponseDto response = paymentService.updatePaymentStatus(id, statusUpdateDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(@RequestBody PaymentProcessDto processDto) {

        PaymentResponseDto response = paymentService.processPayment(processDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @RequestBody PaymentRefundDto refundDto) {

        log.info("Received request to refund payment with ID: {}", refundDto.getPaymentId());

        PaymentResponseDto response = paymentService.refundPayment(refundDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/total-amount")
    public ResponseEntity<BigDecimal> getTotalAmountByStatus(@RequestParam PaymentStatus status) {
        log.info("Received request to get total amount for status: {}", status);

        BigDecimal totalAmount = paymentService.getTotalAmountByStatus(status);
        return ResponseEntity.ok(totalAmount);
    }

    @GetMapping("/statistics/by-method")
    public ResponseEntity<List<Object[]>> getPaymentStatisticsByMethod() {
        log.info("Received request to get payment statistics by method");

        List<Object[]> statistics = paymentService.getPaymentStatisticsByMethod();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/order/{orderId}/exists")
    public ResponseEntity<Boolean> checkPaymentExistsForOrder(
            @PathVariable @Min(value = 1, message = "Order ID must be positive") Integer orderId) {

        log.info("Received request to check if payment exists for order: {}", orderId);

        boolean exists = paymentService.existsByOrderId(orderId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check endpoint called");
        return ResponseEntity.ok("Service is up and running");
    }
}
