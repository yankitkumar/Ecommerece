package com.checkoutline.payment.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(name = "uq_payments_order_id", columnNames = "order_id"))
public class Payment {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    // Unique, not just indexed: this IS the idempotency guard. A redelivered inventory.reserved
    // for an order already charged hits this constraint instead of charging the card twice.
    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider_ref")
    private String providerRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Payment() {
        // JPA
    }

    public Payment(String orderId, BigDecimal amount, PaymentStatus status, String providerRef) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.providerRef = providerRef;
    }

    public void markRefunded() { this.status = PaymentStatus.REFUNDED; }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getProviderRef() { return providerRef; }
    public Instant getCreatedAt() { return createdAt; }
}
