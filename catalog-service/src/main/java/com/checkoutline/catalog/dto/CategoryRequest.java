package com.checkoutline.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name, String parentId) {
}
