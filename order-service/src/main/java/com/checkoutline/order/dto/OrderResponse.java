package com.checkoutline.order.dto;

import com.checkoutline.order.model.Order;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String orderId,
        String userId,
        String status,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt,
        String cancellationReason
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getCancellationReason()
        );
    }
}
