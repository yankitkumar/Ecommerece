package com.checkoutline.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String line1,
        @NotBlank String city,
        @NotBlank String province,
        @NotBlank String postalCode,
        @NotBlank String country,
        boolean isDefault
) {}
