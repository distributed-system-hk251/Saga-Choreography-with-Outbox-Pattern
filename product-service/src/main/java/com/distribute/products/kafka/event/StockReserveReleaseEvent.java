package com.distribute.products.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReserveReleaseEvent(
    Integer orderId,
    String requestId,
    List<Item> items
) {
}
