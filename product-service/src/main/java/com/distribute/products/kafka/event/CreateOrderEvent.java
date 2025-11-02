package com.distribute.products.kafka.event;


import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderEvent {
    Integer orderId;
    Integer userId;
    java.util.List<Item> items;
    String requestId;
    String status;
}
