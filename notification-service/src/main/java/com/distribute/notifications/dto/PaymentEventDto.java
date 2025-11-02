package com.distribute.notifications.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDto {
    
    @JsonProperty("event_type")
    private String eventType;
    
    @JsonProperty("payment_id")
    private Integer paymentId;
    
    @JsonProperty("order_id")
    private Integer orderId;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
