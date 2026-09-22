package com.checkoutline.catalog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A document, not a row: a shirt has size/color, a laptop has RAM/CPU. MongoDB lets
 * `attributes` vary freely per product instead of forcing one sparse relational schema
 * (or an EAV table) to fit every category — see the design doc's database-per-service section.
 */
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String categoryId;
    private List<String> images;
    private Map<String, String> attributes;
    private boolean active = true;
    private Instant createdAt = Instant.now();

    protected Product() {
        // Spring Data
    }

    public Product(String sku, String name, String description, BigDecimal price, String currency,
                    String categoryId, List<String> images, Map<String, String> attributes) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.categoryId = categoryId;
        this.images = images;
        this.attributes = attributes;
    }

    public void updatePrice(BigDecimal price) { this.price = price; }
    public void rename(String name) { this.name = name; }
    public void setActive(boolean active) { this.active = active; }

    public String getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public String getCategoryId() { return categoryId; }
    public List<String> getImages() { return images; }
    public Map<String, String> getAttributes() { return attributes; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
