package com.checkoutline.order.repository;

import com.checkoutline.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
