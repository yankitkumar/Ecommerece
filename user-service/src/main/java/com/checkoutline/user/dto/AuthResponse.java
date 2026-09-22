package com.checkoutline.user.dto;

public record AuthResponse(String token, String userId, String email) {}
