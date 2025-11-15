package com.app.order_service.kafka.event;

import java.util.List;
import com.app.order_service.dto.request.Item;

public record StockReserveSucceededEvent(Integer orderId, List<Item> items) {}

