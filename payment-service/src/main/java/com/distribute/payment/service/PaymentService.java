package com.distribute.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.distribute.payment.dto.PaymentProcessDto;
import com.distribute.payment.dto.PaymentRefundDto;
import com.distribute.payment.dto.PaymentRequestDto;
import com.distribute.payment.dto.PaymentResponseDto;
import com.distribute.payment.dto.PaymentStatusUpdateDto;
import com.distribute.payment.entity.Payment;
import com.distribute.payment.entity.PaymentStatus;
import com.distribute.payment.exception.PaymentNotFoundException;
import com.distribute.payment.exception.PaymentProcessingException;
import com.distribute.payment.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxService outboxService;

    // @Autowired
    // private PaymentProducer paymentProducer;

    public PaymentResponseDto createPayment(PaymentRequestDto requestDto) {
        log.info("Creating payment for order: {}", requestDto.getOrderId());

        try {
            // Create payment entity
            Payment payment = Payment.builder()
                    .orderId(requestDto.getOrderId())
                    .amount(requestDto.getAmount())
                    .method(requestDto.getMethod())
                    .status(PaymentStatus.PENDING)
                    .build();

            // Save payment
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created with ID: {}", savedPayment.getId());

            return convertToResponseDto(savedPayment);

        } catch (Exception e) {
            log.error("Error creating payment for order: {}", requestDto.getOrderId(), e);
            throw new PaymentProcessingException("Failed to create payment: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Integer id) {
        log.info("Retrieving payment with ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));

        return convertToResponseDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByOrderId(Integer orderId) {
        log.info("Retrieving payments for order: {}", orderId);

        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        log.info("Retrieving payments with status: {}", status);

        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
        return payments.map(this::convertToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {
        log.info("Retrieving all payments");

        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(this::convertToResponseDto);
    }

    public PaymentResponseDto updatePaymentStatus(Integer id, PaymentStatusUpdateDto statusUpdateDto) {
        log.info("Updating payment status for ID: {} to {}", id, statusUpdateDto.getStatus());

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(statusUpdateDto.getStatus());

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment status updated from {} to {} for ID: {}", oldStatus, statusUpdateDto.getStatus(), id);

        // Send event to Kafka
        // sendPaymentEvent("PAYMENT_STATUS_UPDATED", updatedPayment);

        return convertToResponseDto(updatedPayment);
    }

    public PaymentResponseDto processPayment(PaymentProcessDto paymentProcessDto) {
        Integer id = paymentProcessDto.getPaymentId();
        log.info("Processing payment with ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentProcessingException(
                    "Payment is not in PENDING status. Current status: " + payment.getStatus());
        }

        try {
            // Simulate payment processing logic
            boolean processingResult = simulatePaymentProcessing(payment);

            if (processingResult) {
                payment.setStatus(PaymentStatus.PAID);
                log.info("Payment processed successfully for ID: {}", id);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Payment processing failed for ID: {}", id);
            }

            payment.setMethod(paymentProcessDto.getMethod());

            Payment updatedPayment = paymentRepository.save(payment);

            // ✅ Save event to outbox (Debezium will publish this to Kafka)
            outboxService.savePaymentProcessedEvent(updatedPayment, id.toString());

            return convertToResponseDto(updatedPayment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            Payment failedPayment = paymentRepository.save(payment);
            
            // ✅ Save failed event to outbox
            outboxService.savePaymentFailedEvent(failedPayment, id.toString(), e.getMessage());
            
            log.error("Error processing payment for ID: {}", id, e);
            throw new PaymentProcessingException("Failed to process payment: " + e.getMessage());
        }
    }

    public PaymentResponseDto refundPayment(PaymentRefundDto refundDto) {
        Integer id = refundDto.getPaymentId();
        String reason = refundDto.getReason();

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new PaymentProcessingException(
                    "Only PAID payments can be refunded. Current status: " + payment.getStatus());
        }

        try {
            // Simulate refund processing
            boolean refundResult = simulateRefundProcessing(payment);

            if (refundResult) {
                payment.setStatus(PaymentStatus.REFUND);
                log.info("Payment refunded successfully for ID: {}", id);
            } else {
                throw new PaymentProcessingException("Refund processing failed");
            }

            Payment updatedPayment = paymentRepository.save(payment);

            // ✅ Save event to outbox (Debezium will publish this to Kafka)
            outboxService.savePaymentRefundedEvent(updatedPayment, id.toString(), reason);

            return convertToResponseDto(updatedPayment);

        } catch (Exception e) {
            log.error("Error processing refund for payment ID: {}", id, e);
            throw new PaymentProcessingException("Failed to process refund: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatus(PaymentStatus status) {
        log.info("Calculating total amount for status: {}", status);
        return paymentRepository.getTotalAmountByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getPaymentStatisticsByMethod() {
        log.info("Retrieving payment statistics by method");
        return paymentRepository.getPaymentStatisticsByMethod();
    }

    @Transactional(readOnly = true)
    public boolean existsByOrderId(Integer orderId) {
        return paymentRepository.existsByOrderId(orderId);
    }

    @Transactional
    public void savePaymentAuthorizeSucceeded(Payment payment) {
        // Save payment and event to outbox in same transaction
        paymentRepository.save(payment);
        outboxService.savePaymentAuthorizeSucceededEvent(payment, payment.getOrderId().toString());
    }

    @Transactional
    public void savePaymentAuthorizeFailed(Payment payment, String reason) {
        // Save payment and event to outbox in same transaction
        paymentRepository.save(payment);
        outboxService.savePaymentAuthorizeFailedEvent(payment, payment.getOrderId().toString(), reason);
    }

    private boolean simulatePaymentProcessing(Payment payment) {
        // Simulate processing logic - in real implementation, this would integrate with
        // payment gateways
        log.info("Simulating payment processing for method: {}, amount: {}",
                payment.getMethod(), payment.getAmount());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate success rate of 90%
        return Math.random() > 0.1;
    }

    private boolean simulateRefundProcessing(Payment payment) {
        // Simulate refund processing logic
        log.info("Simulating refund processing for payment ID: {}, amount: {}",
                payment.getId(), payment.getAmount());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate success rate of 95%
        return Math.random() > 0.05;
    }

    // private void sendPaymentEvent(String eventType, Payment payment) {
    //     try {
    //         // String message =
    //         // String.format("{\"eventType\":\"%s\",\"paymentId\":%d,\"orderId\":%d,\"status\":\"%s\",\"amount\":%.2f}",
    //         // eventType, payment.getId(), payment.getOrderId(), payment.getStatus(),
    //         // payment.getAmount());

    //         // kafkaTemplate.send(PAYMENT_TOPIC, String.valueOf(payment.getId()), message);
    //         log.info("Payment event sent: {} for payment ID: {}", eventType, payment.getId());
    //     } catch (Exception e) {
    //         log.error("Failed to send payment event: {} for payment ID: {}", eventType, payment.getId(), e);
    //     }
    // }

    private PaymentResponseDto convertToResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
