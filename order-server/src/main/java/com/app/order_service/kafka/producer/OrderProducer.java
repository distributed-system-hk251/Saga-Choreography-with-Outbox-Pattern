package com.app.order_service.kafka.producer;

import com.app.order_service.dto.request.Item;
import com.app.order_service.kafka.event.CreateOrderDto;
import com.app.order_service.kafka.event.NotificationSendEvent;
import com.app.order_service.kafka.event.PaymentAuthorizeEvent;
import com.app.order_service.kafka.event.StockReserveSucceededEvent;
import com.app.order_service.kafka.topic.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
// Producer
public class OrderProducer {
    // Template use for transfer data. Can be re-use for multiple topic.
    private final KafkaTemplate<String, String> template;

    // Mapper is used to convert object to String. Or reconvert again from String to
    // Object.
    private final ObjectMapper mapper;

    public void publishNewOrder(Integer userId, Integer orderId, List<Item> items, String requestId)
            throws JsonProcessingException {
        // Create transfer object data
        CreateOrderDto createOrderDto = CreateOrderDto.builder()
                .userId(userId)
                .orderId(orderId)
                .items(items)
                .requestId(requestId)
                .build();

        // Parse Object to String to transfer.
        String jsonParse = mapper.writeValueAsString(createOrderDto);

        // Send data to topic.
        template.send(KafkaTopics.ORDER_CREATED, jsonParse);
    }

    public void publishPaymentAuthorize(Integer orderId, BigDecimal amount) {
        PaymentAuthorizeEvent paymentAuthevent = PaymentAuthorizeEvent.builder()
                .orderId(orderId)
                .amount(amount)
                .build();

        try {
            String jsonParse = mapper.writeValueAsString(paymentAuthevent);
            template.send(KafkaTopics.PAYMENT_AUTHORIZE, jsonParse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to publish payment authorization event", e);
        }

    }

    public void publishNotificationSend(Integer orderId, String type, String message) {
        NotificationSendEvent payload = new NotificationSendEvent(orderId, type, message);

        try {
            String jsonParse = mapper.writeValueAsString(payload);
            template.send(KafkaTopics.NOTIFICATION_SEND, jsonParse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void publishStockReserveRelease(Integer orderId, java.util.List<Item> items) {
        try {
            String payload = mapper.writeValueAsString(new StockReserveSucceededEvent(orderId, items));
            template.send(KafkaTopics.STOCK_RESERVE_RELEASE, payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
