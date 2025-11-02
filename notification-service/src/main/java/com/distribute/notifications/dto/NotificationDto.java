package com.distribute.notifications.dto;

import com.distribute.notifications.entity.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private Integer id;
    
    @NotNull(message = "Order ID is required")
    private Integer orderId;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Message is required")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    public NotificationDto(Integer orderId, String type, String message) {
        this.orderId = orderId;
        this.type = type;
        this.message = message;
    }

    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .orderId(notification.getOrderId())
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public Notification toEntity() {
        return Notification.builder()
                .id(this.id)
                .orderId(this.orderId)
                .type(this.type)
                .message(this.message)
                .build();
    }
}
