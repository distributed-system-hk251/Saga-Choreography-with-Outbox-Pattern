package com.distribute.payment.dto;

import com.distribute.payment.entity.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessDto {

    @NotNull(message = "Payment ID is required")
    @Positive(message = "Payment ID must be positive")
    private Integer paymentId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

}
