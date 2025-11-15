package com.app.order_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CreateOrderForm {
    private Integer userId;
    private List<Item> items;
}
