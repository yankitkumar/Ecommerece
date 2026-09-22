package com.checkoutline.order.controller;

import com.checkoutline.order.dto.CreateOrderRequest;
import com.checkoutline.order.dto.OrderResponse;
import com.checkoutline.order.model.Order;
import com.checkoutline.order.repository.OrderRepository;
import com.checkoutline.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    /**
     * POST, not PUT: the order's id doesn't exist yet — the server generates it, and placing
     * an order isn't naturally idempotent. The optional Idempotency-Key header protects a
     * client retry (e.g. after a network blip) from creating a second order for the same intent.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Order order = orderService.placeOrder(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(OrderResponse.from(order));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No order with id " + orderId));
        return OrderResponse.from(order);
    }
}
