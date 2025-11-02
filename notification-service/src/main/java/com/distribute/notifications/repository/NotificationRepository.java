package com.distribute.notifications.repository;

import com.distribute.notifications.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    List<Notification> findByOrderId(Integer orderId);

    Optional<Notification> findById(Integer id);

    List<Notification> findByType(String type);

    Page<Notification> findAll(Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT n FROM Notification n WHERE n.orderId = :orderId AND n.type = :type")
    List<Notification> findByOrderIdAndType(@Param("orderId") Integer orderId, @Param("type") String type);
    
    Long countByType(String type);
    
    void deleteByOrderId(Integer orderId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :date")
    Long countNotificationsAfterDate(@Param("date") LocalDateTime date);
}
    