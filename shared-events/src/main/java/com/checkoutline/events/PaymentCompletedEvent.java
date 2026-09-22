package com.checkoutline.events;

import java.math.BigDecimal;

/** Published by Payment Service once a charge succeeds. */
public record PaymentCompletedEvent(String orderId, String paymentId, BigDecimal amount) {}
