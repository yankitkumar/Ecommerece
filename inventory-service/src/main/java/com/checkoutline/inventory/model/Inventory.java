package com.checkoutline.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @Column(name = "product_id", length = 64)
    private String productId;

    @Column(name = "available_qty", nullable = false)
    private int availableQty;

    @Column(name = "reserved_qty", nullable = false)
    private int reservedQty;

    // Optimistic lock: two concurrent reservations for the last unit of stock must not both
    // succeed. Hibernate bumps this on every UPDATE and rejects a write based on stale data.
    @Version
    private long version;

    protected Inventory() {
        // JPA
    }

    public Inventory(String productId, int availableQty) {
        this.productId = productId;
        this.availableQty = availableQty;
        this.reservedQty = 0;
    }

    public boolean reserve(int qty) {
        if (availableQty < qty) {
            return false;
        }
        availableQty -= qty;
        reservedQty += qty;
        return true;
    }

    public void addStock(int qty) {
        availableQty += qty;
    }

    public void release(int qty) {
        reservedQty = Math.max(0, reservedQty - qty);
        availableQty += qty;
    }

    public String getProductId() { return productId; }
    public int getAvailableQty() { return availableQty; }
    public int getReservedQty() { return reservedQty; }
    public long getVersion() { return version; }
}
