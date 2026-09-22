package com.checkoutline.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotNull @Positive BigDecimal price,
        @NotBlank String currency,
        String categoryId,
        List<String> images,
        Map<String, String> attributes
) {}
