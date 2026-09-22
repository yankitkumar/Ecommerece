package com.checkoutline.order.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private String productId;

    // Snapshot at purchase time — later catalog edits never rewrite this order's history.
    @Column(name = "name_snapshot", nullable = false)
    private String nameSnapshot;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {
        // JPA
    }

    public OrderItem(String productId, String nameSnapshot, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.nameSnapshot = nameSnapshot;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    void setOrder(Order order) { this.order = order; }

    public String getId() { return id; }
    public Order getOrder() { return order; }
    public String getProductId() { return productId; }
    public String getNameSnapshot() { return nameSnapshot; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
