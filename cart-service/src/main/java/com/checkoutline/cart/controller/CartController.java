package com.checkoutline.cart.controller;

import com.checkoutline.cart.model.CartItem;
import com.checkoutline.cart.service.CartService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public List<CartItem> getCart(@PathVariable String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addItem(@PathVariable String userId, @RequestBody AddItemRequest request) {
        return cartService.addItem(userId, request.productId(), request.quantity());
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable String userId, @PathVariable String productId) {
        cartService.removeItem(userId, productId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
    }

    public record AddItemRequest(@NotBlank String productId, @Min(1) int quantity) {}
}
