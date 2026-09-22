package com.checkoutline.events;

import java.math.BigDecimal;
import java.util.List;

/** Published by Order Service the moment an order is persisted as PENDING. Key: orderId. */
public record OrderCreatedEvent(
        String orderId,
        String userId,
        List<OrderItemPayload> items,
        BigDecimal totalAmount,
        String currency
) {}
