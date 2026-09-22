package com.checkoutline.inventory.repository;

import com.checkoutline.inventory.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockReservationRepository extends JpaRepository<StockReservation, String> {
    List<StockReservation> findByOrderId(String orderId);
    Optional<StockReservation> findByOrderIdAndProductId(String orderId, String productId);
    boolean existsByOrderId(String orderId);
}
