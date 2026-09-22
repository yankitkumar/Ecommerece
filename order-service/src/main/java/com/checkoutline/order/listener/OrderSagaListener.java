package com.checkoutline.order.listener;

import com.checkoutline.events.InventoryFailedEvent;
import com.checkoutline.events.OrderCancelledEvent;
import com.checkoutline.events.OrderConfirmedEvent;
import com.checkoutline.events.PaymentCompletedEvent;
import com.checkoutline.events.PaymentFailedEvent;
import com.checkoutline.events.Topics;
import com.checkoutline.order.model.Order;
import com.checkoutline.order.model.OrderStatus;
import com.checkoutline.order.outbox.OutboxEvent;
import com.checkoutline.order.repository.OrderRepository;
import com.checkoutline.order.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order Service's half of the choreographed saga. Every handler is idempotent — Kafka is
 * at-least-once delivery, so a duplicate payment.completed must not confirm an order twice —
 * and every status change plus its resulting event write to the SAME local transaction as
 * the outbox row, exactly like OrderService.placeOrder does for order creation.
 */
@Component
public class OrderSagaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaListener.class);

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OrderSagaListener(OrderRepository orderRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.PAYMENT_COMPLETED, groupId = "order-service")
    @Transactional
    public void onPaymentCompleted(String payload) throws Exception {
        var event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            log.warn("payment.completed for unknown order {} — ignoring", event.orderId());
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("Order {} already {} — ignoring duplicate payment.completed", order.getId(), order.getStatus());
            return; // idempotent: already confirmed (or cancelled) by an earlier delivery
        }
        order.confirm();
        orderRepository.save(order);
        writeOutbox(order.getId(), Topics.ORDER_CONFIRMED, new OrderConfirmedEvent(order.getId(), order.getUserId()));
        log.info("Order {} CONFIRMED", order.getId());
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "order-service")
    @Transactional
    public void onPaymentFailed(String payload) throws Exception {
        var event = objectMapper.readValue(payload, PaymentFailedEvent.class);
        cancel(event.orderId(), "Payment failed: " + event.reason());
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.INVENTORY_FAILED, groupId = "order-service")
    @Transactional
    public void onInventoryFailed(String payload) throws Exception {
        var event = objectMapper.readValue(payload, InventoryFailedEvent.class);
        cancel(event.orderId(), "Out of stock: " + event.reason());
    }

    /** Catches messages that exhausted all retries above — logged for manual replay/inspection. */
    @DltHandler
    public void onDlt(String payload) {
        log.error("Message sent to DLT after exhausting retries, payload={}", payload);
    }

    private void cancel(String orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Cancellation for unknown order {} — ignoring", orderId);
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("Order {} already {} — ignoring duplicate cancellation trigger", order.getId(), order.getStatus());
            return;
        }
        order.cancel(reason);
        orderRepository.save(order);
        writeOutbox(order.getId(), Topics.ORDER_CANCELLED, new OrderCancelledEvent(order.getId(), order.getUserId(), reason));
        log.info("Order {} CANCELLED: {}", order.getId(), reason);
    }

    private void writeOutbox(String orderId, String eventType, Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(new OutboxEvent(orderId, eventType, json));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize outbox event " + eventType, e);
        }
    }
}
