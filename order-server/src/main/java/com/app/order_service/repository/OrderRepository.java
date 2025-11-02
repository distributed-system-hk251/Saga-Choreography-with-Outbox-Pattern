package com.app.order_service.repository;

import com.app.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    java.util.List<Order> findByUserId(Integer userId);
}
