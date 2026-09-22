package com.checkoutline.events;

import java.math.BigDecimal;

/** A line item as it existed at checkout time — a price/name snapshot, not a live catalog lookup. */
public record OrderItemPayload(
        String productId,
        String nameSnapshot,
        int quantity,
        BigDecimal unitPrice
) {}
