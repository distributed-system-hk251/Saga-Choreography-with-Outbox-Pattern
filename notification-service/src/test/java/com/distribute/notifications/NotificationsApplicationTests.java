package com.distribute.notifications;

import com.distribute.notifications.dto.NotificationDto;
import com.distribute.notifications.repository.NotificationRepository;
import com.distribute.notifications.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringJUnitConfig
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.kafka.consumer.bootstrap-servers=",
    "spring.kafka.producer.bootstrap-servers=",
    "eureka.client.enabled=false"
})
@Transactional
class NotificationsApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void contextLoads() {
        assertThat(notificationService).isNotNull();
        assertThat(notificationRepository).isNotNull();
    }

    @Test
    void testCreateNotification() {
        // Given
        NotificationDto notificationDto = NotificationDto.builder()
                .orderId(1001)
                .type("info")
                .message("Test notification")
                .build();

        // When
        NotificationDto created = notificationService.createNotification(notificationDto);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getOrderId()).isEqualTo(1001);
        assertThat(created.getType()).isEqualTo("info");
        assertThat(created.getMessage()).isEqualTo("Test notification");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void testGetNotificationsByOrderId() {
        // Given
        NotificationDto notification1 = NotificationDto.builder()
                .orderId(2001)
                .type("info")
                .message("Order created")
                .build();

        NotificationDto notification2 = NotificationDto.builder()
                .orderId(2001)
                .type("alert")
                .message("Payment successful")
                .build();

        notificationService.createNotification(notification1);
        notificationService.createNotification(notification2);

        // When
        List<NotificationDto> notifications = notificationService.getNotificationsByOrderId(2001);

        // Then
        assertThat(notifications).hasSize(2);
        assertThat(notifications.stream().allMatch(n -> n.getOrderId().equals(2001))).isTrue();
    }

    @Test
    void testGetNotificationsByType() {
        // Given
        NotificationDto notification1 = NotificationDto.builder()
                .orderId(3001)
                .type("info")
                .message("Order created 1")
                .build();

        NotificationDto notification2 = NotificationDto.builder()
                .orderId(3002)
                .type("info")
                .message("Order created 2")
                .build();

        NotificationDto notification3 = NotificationDto.builder()
                .orderId(3003)
                .type("alert")
                .message("Payment successful")
                .build();

        notificationService.createNotification(notification1);
        notificationService.createNotification(notification2);
        notificationService.createNotification(notification3);

        // When
        List<NotificationDto> infoNotifications = 
                notificationService.getNotificationsByType("info");

        // Then
        assertThat(infoNotifications).hasSize(2);
        assertThat(infoNotifications.stream()
                .allMatch(n -> n.getType().equals("info"))).isTrue();
    }

    @Test
    void testUpdateNotification() {
        // Given
        NotificationDto notificationDto = NotificationDto.builder()
                .orderId(4001)
                .type("info")
                .message("Original message")
                .build();

        NotificationDto created = notificationService.createNotification(notificationDto);

        // When
        NotificationDto updateDto = NotificationDto.builder()
                .orderId(4001)
                .type("alert")
                .message("Updated message")
                .build();

        NotificationDto updated = notificationService.updateNotification(created.getId(), updateDto);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getType()).isEqualTo("alert");
        assertThat(updated.getMessage()).isEqualTo("Updated message");
    }

    @Test
    void testDeleteNotification() {
        // Given
        NotificationDto notificationDto = NotificationDto.builder()
                .orderId(5001)
                .type("error")
                .message("Order cancelled")
                .build();

        NotificationDto created = notificationService.createNotification(notificationDto);

        // When
        boolean deleted = notificationService.deleteNotification(created.getId());

        // Then
        assertThat(deleted).isTrue();
        
        // Verify it's deleted
        NotificationDto found = notificationService.getNotificationById(created.getId());
        assertThat(found).isNull();
    }

    @Test
    void testGetNotificationById() {
        // Given
        NotificationDto notificationDto = NotificationDto.builder()
                .orderId(6001)
                .type("warning")
                .message("Test message")
                .build();

        NotificationDto created = notificationService.createNotification(notificationDto);

        // When
        NotificationDto found = notificationService.getNotificationById(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getOrderId()).isEqualTo(6001);
        assertThat(found.getType()).isEqualTo("warning");
        assertThat(found.getMessage()).isEqualTo("Test message");
    }

    @Test
    void testHealthEndpoint() {
        // When
        String url = "http://localhost:" + port + "/api/v1/notifications/health";
        String response = restTemplate.getForObject(url, String.class);
        
        // Then
        assertThat(response).contains("status");
        assertThat(response).contains("Notification Service");
    }

    @Test
    void testGetNotificationCountByType() {
        // Given
        NotificationDto notification1 = NotificationDto.builder()
                .orderId(7001)
                .type("info")
                .message("Info message 1")
                .build();

        NotificationDto notification2 = NotificationDto.builder()
                .orderId(7002)
                .type("info")
                .message("Info message 2")
                .build();

        NotificationDto notification3 = NotificationDto.builder()
                .orderId(7003)
                .type("alert")
                .message("Alert message")
                .build();

        notificationService.createNotification(notification1);
        notificationService.createNotification(notification2);
        notificationService.createNotification(notification3);

        // When
        Long infoCount = notificationService.getNotificationCountByType("info");
        Long alertCount = notificationService.getNotificationCountByType("alert");

        // Then
        assertThat(infoCount).isGreaterThanOrEqualTo(2);
        assertThat(alertCount).isGreaterThanOrEqualTo(1);
    }
}
