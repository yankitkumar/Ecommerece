package com.checkoutline.inventory.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * One row per (order, product) reservation. Its existence is also the idempotency guard for
 * the order.created listener: if a reservation already exists for this order+product, a
 * redelivered event is a no-op instead of double-reserving stock.
 */
@Entity
@Table(name = "stock_reservations", uniqueConstraints =
    @UniqueConstraint(name = "uq_reservation_order_product", columnNames = {"order_id", "product_id"}))
public class StockReservation {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected StockReservation() {
        // JPA
    }

    public StockReservation(String orderId, String productId, int quantity, ReservationStatus status) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
    }

    public void markReleased() { this.status = ReservationStatus.RELEASED; }
    public void markCommitted() { this.status = ReservationStatus.COMMITTED; }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
