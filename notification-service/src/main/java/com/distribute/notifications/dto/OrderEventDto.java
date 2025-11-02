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
public class OrderEventDto {
    
    @JsonProperty("event_type")
    private String eventType;
    
    @JsonProperty("order_id")
    private Integer orderId;
    
    @JsonProperty("user_id")
    private Integer userId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
