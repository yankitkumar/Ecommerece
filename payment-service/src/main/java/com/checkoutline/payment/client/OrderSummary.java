package com.checkoutline.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** The subset of Order Service's GET /orders/{id} response Payment actually needs. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderSummary(String orderId, BigDecimal totalAmount, String currency) {}
