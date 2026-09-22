package com.checkoutline.order.service;

import com.checkoutline.events.OrderCreatedEvent;
import com.checkoutline.events.OrderItemPayload;
import com.checkoutline.events.Topics;
import com.checkoutline.order.dto.CreateOrderRequest;
import com.checkoutline.order.dto.OrderItemRequest;
import com.checkoutline.order.model.Order;
import com.checkoutline.order.model.OrderItem;
import com.checkoutline.order.outbox.OutboxEvent;
import com.checkoutline.order.repository.OrderRepository;
import com.checkoutline.order.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates an order and its outbox row in ONE local transaction. If the idempotency key
     * has already been used, the original order is returned instead of creating a duplicate —
     * this is what makes a retried POST /orders safe.
     */
    @Transactional
    public Order placeOrder(CreateOrderRequest request, String idempotencyKey) {
        if (idempotencyKey != null) {
            var existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotency key {} already used — returning existing order {}", idempotencyKey, existing.get().getId());
                return existing.get();
            }
        }

        BigDecimal total = request.items().stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(request.userId(), total, request.currency(), request.shippingAddressJson(), idempotencyKey);
        for (OrderItemRequest item : request.items()) {
            order.addItem(new OrderItem(item.productId(), item.nameSnapshot(), item.unitPrice(), item.quantity()));
        }
        order = orderRepository.save(order);

        publishToOutbox(order);
        log.info("Order {} saved as PENDING with {} items", order.getId(), order.getItems().size());
        return order;
    }

    private void publishToOutbox(Order order) {
        List<OrderItemPayload> items = order.getItems().stream()
                .map(i -> new OrderItemPayload(i.getProductId(), i.getNameSnapshot(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        var event = new OrderCreatedEvent(order.getId(), order.getUserId(), items, order.getTotalAmount(), order.getCurrency());
        String payload = writeJson(event);
        outboxEventRepository.save(new OutboxEvent(order.getId(), Topics.ORDER_CREATED, payload));
    }

    private String writeJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            // A serialization failure here means a bug in the event contract, not a transient
            // fault — fail the transaction rather than silently dropping the outbox row.
            throw new IllegalStateException("Failed to serialize outbox event", e);
        }
    }
}
