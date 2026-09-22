package com.checkoutline.events;

/** Published by Order Service when the saga could not complete — carries why, for the customer email. */
public record OrderCancelledEvent(String orderId, String userId, String reason) {}
