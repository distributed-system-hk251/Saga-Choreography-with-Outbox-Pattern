package com.distribute.products.dto;

/**
 * Order Status Flow in Saga Choreography:
 * 
 * 1. PENDING - Order just created, waiting for stock reservation
 * 2. STOCK_RESERVED - Product service confirmed stock availability
 * 3. STOCK_FAILED - Product reservation failed (insufficient stock)
 * 4. PAYMENT_PENDING - Waiting for payment service
 * 5. PAID - Payment successful
 * 6. PAYMENT_FAILED - Payment declined
 * 7. COMPLETED - Order successfully finished (after sending notification)
 * 8. CANCELED - Order canceled (by user or system)
 * 9. REFUNDED - Payment refunded
 * 
 * Product Service Actions:
 * - PENDING → Reserve stock (decrease quantity)
 * - PAYMENT_FAILED → Release stock (increase quantity)
 * - CANCELED → Release stock (increase quantity)
 * - STOCK_RESERVE_RELEASE event → Release stock (increase quantity)
 */
public class OrderStatusFlow {
    
    public static final String PENDING = "PENDING";
    public static final String STOCK_RESERVED = "STOCK_RESERVED";
    public static final String STOCK_FAILED = "STOCK_FAILED";
    public static final String PAYMENT_PENDING = "PAYMENT_PENDING";
    public static final String PAID = "PAID";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELED = "CANCELED";
    public static final String REFUNDED = "REFUNDED";
    
    /**
     * Check if order status requires stock reservation
     */
    public static boolean requiresStockReservation(String status) {
        return PENDING.equals(status);
    }
    
    /**
     * Check if order status requires stock release
     */
    public static boolean requiresStockRelease(String status) {
        return PAYMENT_FAILED.equals(status) || 
               CANCELED.equals(status) ||
               REFUNDED.equals(status);
    }
}
