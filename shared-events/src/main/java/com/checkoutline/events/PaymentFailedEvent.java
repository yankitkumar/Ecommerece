package com.checkoutline.events;

/** Published by Payment Service when a charge is declined or errors out. Triggers compensation. */
public record PaymentFailedEvent(String orderId, String reason) {}
