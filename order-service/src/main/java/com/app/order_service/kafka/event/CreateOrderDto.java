package com.app.order_service.kafka.event;

import com.app.order_service.dto.request.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderDto {
    private Integer userId;
    private Integer orderId;
    private List<Item> items;
    private String requestId;
}
