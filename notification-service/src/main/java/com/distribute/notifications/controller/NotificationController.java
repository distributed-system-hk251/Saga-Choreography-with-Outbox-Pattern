package com.distribute.notifications.controller;

import com.distribute.notifications.dto.NotificationDto;
import com.distribute.notifications.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    /** 
     * Get notifications by order ID
     * GET /api/v1/notifications/order/{orderId}
    */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByOrderId(@PathVariable Integer orderId) {
        logger.info("Fetching notifications for order ID: {}", orderId);
        try {
            List<NotificationDto> notifications = notificationService.getNotificationsByOrderId(orderId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching notifications for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notifications by id
     * GET /api/v1/notifications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Integer id) {
        logger.info("Fetching notification with ID: {}", id);
        try {
            NotificationDto notification = notificationService.getNotificationById(id);
            if (notification != null) {
                return ResponseEntity.ok(notification);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("Error fetching notification with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new notification
     * POST /api/v1/notifications/create
     */
    @PostMapping("/create")
    public ResponseEntity<NotificationDto> createNotification(@Valid @RequestBody NotificationDto notificationDto) {
        logger.info("Creating new notification");
        try {
            NotificationDto createdNotification = notificationService.createNotification(notificationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
        } catch (Exception e) {
            logger.error("Error creating notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get notification by type
     * GET /api/v1/notifications/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByType(@PathVariable String type) {
        logger.info("Fetching notifications of type: {}", type);
        try {
            List<NotificationDto> notifications = notificationService.getNotificationsByType(type);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching notifications of type: {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all notifications with pagination
     * GET /api/v1/notifications?page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("Fetching all notifications - page: {}, size: {}", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDto> notificationPage = notificationService.getAllNotifications(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationPage.getContent());
            response.put("currentPage", notificationPage.getNumber());
            response.put("totalItems", notificationPage.getTotalElements());
            response.put("totalPages", notificationPage.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching notifications - page: {}, size: {}", page, size, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete notification by ID
     * DELETE /api/v1/notifications/{id}/delete
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer id) {
        logger.info("Deleting notification with ID: {}", id);
        try {
            boolean deleted = notificationService.deleteNotification(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("Error deleting notification with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete notification by order ID
     * DELETE /api/v1/notifications/order/{orderId}/delete
     */
    @DeleteMapping("/order/{orderId}/delete")
    public ResponseEntity<Void> deleteNotificationsByOrderId(@PathVariable Integer orderId) {
        logger.info("Deleting notifications for order ID: {}", orderId);
        try {
            List<NotificationDto> notifications = notificationService.getNotificationsByOrderId(orderId);
            if (notifications.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            for (NotificationDto notification : notifications) {
                notificationService.deleteNotification(notification.getId());
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting notifications for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update notification by ID
     * PUT /api/v1/notifications/{id}/update 
    */
    @PutMapping("/{id}/update")
    public ResponseEntity<NotificationDto> updateNotification(
            @PathVariable Integer id, 
            @Valid @RequestBody NotificationDto notificationDto) {
        logger.info("Updating notification with ID: {}", id);
        try {
            NotificationDto existingNotification = notificationService.getNotificationById(id);
            if (existingNotification == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            notificationDto.setId(id);
            NotificationDto updatedNotification = notificationService.createNotification(notificationDto);
            return ResponseEntity.ok(updatedNotification);
        } catch (Exception e) {
            logger.error("Error updating notification with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update notification by order ID
     * PUT /api/v1/notifications/order/{orderId}/update
     */
    @PutMapping("/order/{orderId}/update")
    public ResponseEntity<List<NotificationDto>> updateNotificationsByOrderId(
            @PathVariable Integer orderId,
            @Valid @RequestBody List<NotificationDto> notificationDtos) {
        logger.info("Updating notifications for order ID: {}", orderId);
        try {
            List<NotificationDto> existingNotifications = notificationService.getNotificationsByOrderId(orderId);
            if (existingNotifications.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            for (NotificationDto notificationDto : notificationDtos) {
                notificationDto.setOrderId(orderId);
                notificationService.createNotification(notificationDto);
            }
            return ResponseEntity.ok(notificationDtos);
        } catch (Exception e) {
            logger.error("Error updating notifications for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification Service is up and running!");
    }
}
