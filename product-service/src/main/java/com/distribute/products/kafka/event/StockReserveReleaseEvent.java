package com.distribute.products.kafka.event;

import java.util.List;

public record StockReserveReleaseEvent(
    Integer orderId,
    List<Item> items
) {
}
