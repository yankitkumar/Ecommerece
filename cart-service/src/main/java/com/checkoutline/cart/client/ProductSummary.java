package com.checkoutline.cart.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSummary(String id, String name, BigDecimal price, boolean active) {}
