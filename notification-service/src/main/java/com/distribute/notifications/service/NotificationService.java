package com.distribute.notifications.service;

import com.distribute.notifications.dto.NotificationDto;
import com.distribute.notifications.entity.Notification;
import com.distribute.notifications.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Get notifications by order ID
     */
    public List<NotificationDto> getNotificationsByOrderId(Integer orderId) {
        logger.debug("Fetching notifications for order ID: {}", orderId);
        List<Notification> notifications = notificationRepository.findByOrderId(orderId);
        return notifications.stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get notification by ID
     */
    public NotificationDto getNotificationById(Integer id) {
        logger.debug("Fetching notification with ID: {}", id);
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        return notificationOpt.map(NotificationDto::fromEntity).orElse(null);
    }

    /**
     * Create a new notification
     */
    public NotificationDto createNotification(NotificationDto notificationDto) {
        logger.info("Creating notification for order: {}", notificationDto.getOrderId());
        Notification notification = notificationDto.toEntity();
        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Successfully created notification with ID: {}", savedNotification.getId());
        return NotificationDto.fromEntity(savedNotification);
    }

    /**
     * Update an existing notification
     */
    public NotificationDto updateNotification(Integer id, NotificationDto notificationDto) {
        logger.info("Updating notification with ID: {}", id);
        Optional<Notification> existingNotificationOpt = notificationRepository.findById(id);

        if (existingNotificationOpt.isPresent()) {
            Notification existingNotification = existingNotificationOpt.get();
            existingNotification.setType(notificationDto.getType());
            existingNotification.setMessage(notificationDto.getMessage());

            Notification updatedNotification = notificationRepository.save(existingNotification);
            logger.info("Successfully updated notification with ID: {}", id);
            return NotificationDto.fromEntity(updatedNotification);
        }

        logger.warn("Notification with ID {} not found for update", id);
        return null;
    }

    /**
     * Get notifications by type
     */
    public List<NotificationDto> getNotificationsByType(String type) {
        logger.debug("Fetching notifications of type: {}", type);
        List<Notification> notifications = notificationRepository.findByType(type);
        return notifications.stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications by order ID and type
     */
    public List<NotificationDto> getNotificationsByOrderIdAndType(Integer orderId, String type) {
        logger.debug("Fetching notifications for order ID: {} and type: {}", orderId, type);
        List<Notification> notifications = notificationRepository.findByOrderIdAndType(orderId, type);
        return notifications.stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all notifications with pagination
     */
    public Page<NotificationDto> getAllNotifications(Pageable pageable) {
        logger.debug("Fetching all notifications with pagination: {}", pageable);
        Page<Notification> notificationsPage = notificationRepository.findAll(pageable);
        return notificationsPage.map(NotificationDto::fromEntity);
    }

    /**
     * Get notifications within date range
     */
    public List<NotificationDto> getNotificationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching notifications between {} and {}", startDate, endDate);
        List<Notification> notifications = notificationRepository.findByCreatedAtBetween(startDate, endDate);
        return notifications.stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Delete notification by ID
     */
    public boolean deleteNotification(Integer id) {
        logger.info("Deleting notification with ID: {}", id);
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            logger.info("Successfully deleted notification with ID: {}", id);
            return true;
        }
        logger.warn("Notification with ID {} not found for deletion", id);
        return false;
    }

    /**
     * Delete notifications by order ID
     */
    @Transactional
    public void deleteNotificationsByOrderId(Integer orderId) {
        logger.info("Deleting all notifications for order ID: {}", orderId);
        notificationRepository.deleteByOrderId(orderId);
        logger.info("Successfully deleted all notifications for order ID: {}", orderId);
    }

    /**
     * Get notification count by type
     */
    public Long getNotificationCountByType(String type) {
        logger.debug("Counting notifications of type: {}", type);
        return notificationRepository.countByType(type);
    }

    /**
     * Get recent notifications count (last 24 hours)
     */
    public Long getRecentNotificationsCount() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        logger.debug("Counting notifications after: {}", yesterday);
        return notificationRepository.countNotificationsAfterDate(yesterday);
    }
}
