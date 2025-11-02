package com.distribute.payment.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.distribute.payment.entity.Payment;
import com.distribute.payment.entity.PaymentMethod;
import com.distribute.payment.entity.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Find payments by order ID
    List<Payment> findByOrderId(Integer orderId);
    
    // Find payments by status
    List<Payment> findByStatus(PaymentStatus status);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    // Find payments by method
    List<Payment> findByMethod(PaymentMethod method);
    
    // Find payments by order ID and status
    List<Payment> findByOrderIdAndStatus(Integer orderId, PaymentStatus status);
    
    // Find payments within amount range
    List<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find payments created within date range
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find payments by multiple criteria
    @Query("SELECT p FROM Payment p WHERE " +
           "(:orderId IS NULL OR p.orderId = :orderId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:method IS NULL OR p.method = :method) AND " +
           "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR p.amount <= :maxAmount)")
    Page<Payment> findPaymentsByCriteria(
            @Param("orderId") Integer orderId,
            @Param("status") PaymentStatus status,
            @Param("method") PaymentMethod method,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);
    
    // Get total amount by status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") PaymentStatus status);
    
    // Get payment statistics by method
    @Query("SELECT p.method, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p GROUP BY p.method")
    List<Object[]> getPaymentStatisticsByMethod();
    
    // Find recent payments
    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC")
    Page<Payment> findRecentPayments(Pageable pageable);
    
    // Check if payment exists for order
    boolean existsByOrderId(Integer orderId);
    
    // Count payments by status
    long countByStatus(PaymentStatus status);
    
    // Find failed payments that can be retried
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt > :sinceDate")
    List<Payment> findFailedPaymentsSince(@Param("sinceDate") LocalDateTime sinceDate);
}