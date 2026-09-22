package com.checkoutline.cart.model;

import java.math.BigDecimal;

/** One line in a cart. Stored as a JSON value under the cart's Redis hash, keyed by productId. */
public record CartItem(String productId, String name, BigDecimal priceSnapshot, int quantity) {

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(productId, name, priceSnapshot, newQuantity);
    }
}
