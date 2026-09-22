package com.checkoutline.order.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(name = "uq_orders_idempotency_key", columnNames = "idempotency_key"))
public class Order {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    // Snapshot of the shipping address at checkout time, stored as JSON text.
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    // The idempotency key the client sent with POST /orders — protects against double-submit retries.
    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
        // JPA
    }

    public Order(String userId, BigDecimal totalAmount, String currency, String shippingAddress, String idempotencyKey) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.shippingAddress = shippingAddress;
        this.idempotencyKey = idempotencyKey;
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    // --- getters (no setters beyond the state-transition methods above — status changes go through them) ---

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getShippingAddress() { return shippingAddress; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCancellationReason() { return cancellationReason; }
    public List<OrderItem> getItems() { return items; }
}
