package com.distribute.payment.dto;

import com.distribute.payment.entity.PaymentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusUpdateDto {
    
    @NotNull(message = "Status is required")
    private PaymentStatus status;
    
    private String reason; // For failed payments or other status changes
}