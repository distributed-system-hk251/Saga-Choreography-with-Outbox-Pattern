package com.distribute.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.distribute.payment.entity.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEventDto {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("orderId")
    private Integer orderId;
    
    @JsonProperty("customerId")
    private Integer customerId;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("paymentMethod")
    private PaymentMethod paymentMethod;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional fields that might be useful
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("currency")
    private String currency;
    
    // Helper method to check if this is an order creation event
    public boolean isOrderCreatedEvent() {
        return "ORDER_CREATED".equalsIgnoreCase(eventType);
    }
    
    // Helper method to check if this is an order confirmed event
    public boolean isOrderConfirmedEvent() {
        return "ORDER_CONFIRMED".equalsIgnoreCase(eventType);
    }
    
    // Helper method to check if payment should be created
    public boolean shouldCreatePayment() {
        return isOrderCreatedEvent() || isOrderConfirmedEvent();
    }
    
    // Helper method to get payment method with fallback
    public PaymentMethod getPaymentMethodWithFallback() {
        return paymentMethod != null ? paymentMethod : PaymentMethod.CARD_PAYMENT;
    }
}