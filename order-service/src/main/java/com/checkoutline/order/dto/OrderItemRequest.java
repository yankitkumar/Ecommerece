package com.checkoutline.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank String productId,
        @NotBlank String nameSnapshot,
        @Min(1) int quantity,
        BigDecimal unitPrice
) {}
