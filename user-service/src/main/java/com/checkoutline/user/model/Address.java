package com.checkoutline.user.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String line1;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String province;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    protected Address() {
        // JPA
    }

    public Address(String userId, String line1, String city, String province, String postalCode, String country, boolean isDefault) {
        this.userId = userId;
        this.line1 = line1;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.country = country;
        this.isDefault = isDefault;
    }

    /** Full replace of every field — backs the idempotent PUT /users/me/addresses/{id}. */
    public void replace(String line1, String city, String province, String postalCode, String country, boolean isDefault) {
        this.line1 = line1;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.country = country;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getLine1() { return line1; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public boolean isDefault() { return isDefault; }
}
