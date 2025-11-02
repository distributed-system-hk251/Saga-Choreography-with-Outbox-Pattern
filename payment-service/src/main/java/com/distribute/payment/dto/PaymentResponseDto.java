package com.distribute.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.distribute.payment.entity.PaymentMethod;
import com.distribute.payment.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    
    private Integer id;
    private Integer orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private String description;
}