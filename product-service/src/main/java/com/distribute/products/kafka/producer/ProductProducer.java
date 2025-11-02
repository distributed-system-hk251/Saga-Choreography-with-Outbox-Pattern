package com.distribute.products.kafka.producer;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import com.distribute.products.kafka.event.*;
import com.distribute.products.kafka.topic.KafkaTopics;

@Component
@AllArgsConstructor
public class ProductProducer {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public void publishStockReserveSucceeded(Integer orderId, List<Item> items) {
        var event = new StockReserveSucceededEvent(orderId, items);
        try {
            String eventString = mapper.writeValueAsString(event);
            kafka.send(KafkaTopics.STOCK_RESERVE_SUCCEEDED, eventString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void publishStockReserveFailed(Integer orderId, String reason) {
        var event = new StockReserveFailedEvent(orderId, reason);
        try {
            String eventString = mapper.writeValueAsString(event);
            kafka.send(KafkaTopics.STOCK_RESERVE_FAILED, eventString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
