package com.distribute.products.kafka.event;

import java.util.List;

public record StockReserveSucceededEvent(Integer orderId, List<Item> items) {}

