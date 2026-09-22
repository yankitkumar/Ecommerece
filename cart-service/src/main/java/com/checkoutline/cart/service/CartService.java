package com.checkoutline.cart.service;

import com.checkoutline.cart.client.CatalogClient;
import com.checkoutline.cart.model.CartItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cart lives entirely in Redis as a hash: key cart:{userId}, field productId, value the
 * CartItem JSON. A hash (not one key per item) means the whole cart is fetched or expired
 * in one shot — see the design doc's Cart Service table.
 */
@Service
public class CartService {

    private static final Duration CART_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;
    private final CatalogClient catalogClient;
    private final ObjectMapper objectMapper;

    public CartService(StringRedisTemplate redisTemplate, CatalogClient catalogClient, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.catalogClient = catalogClient;
        this.objectMapper = objectMapper;
    }

    public List<CartItem> getCart(String userId) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(cartKey(userId));
        return raw.values().stream().map(this::readItem).collect(Collectors.toList());
    }

    public CartItem addItem(String userId, String productId, int quantity) {
        var product = catalogClient.getProduct(productId);
        if (product == null || !product.active()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product " + productId + " is not available");
        }
        CartItem item = new CartItem(productId, product.name(), product.price(), quantity);
        String key = cartKey(userId);
        redisTemplate.opsForHash().put(key, productId, writeItem(item));
        redisTemplate.expire(key, CART_TTL); // refresh TTL on every add — resets the abandonment clock
        return item;
    }

    public void removeItem(String userId, String productId) {
        redisTemplate.opsForHash().delete(cartKey(userId), productId);
    }

    public void clearCart(String userId) {
        redisTemplate.delete(cartKey(userId));
    }

    private String cartKey(String userId) {
        return "cart:" + userId;
    }

    private String writeItem(CartItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize cart item", e);
        }
    }

    private CartItem readItem(Object json) {
        try {
            return objectMapper.readValue((String) json, CartItem.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize cart item", e);
        }
    }
}
