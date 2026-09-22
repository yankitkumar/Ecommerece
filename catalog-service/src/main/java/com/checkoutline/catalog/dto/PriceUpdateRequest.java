package com.checkoutline.catalog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PriceUpdateRequest(@NotNull @Positive BigDecimal price) {}
