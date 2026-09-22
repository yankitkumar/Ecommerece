package com.checkoutline.events;

/** Published by Order Service once payment has cleared and the order is CONFIRMED. */
public record OrderConfirmedEvent(String orderId, String userId) {}
