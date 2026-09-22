package com.checkoutline.inventory.listener;

import com.checkoutline.events.OrderCreatedEvent;
import com.checkoutline.events.PaymentFailedEvent;
import com.checkoutline.events.Topics;
import com.checkoutline.inventory.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class InventorySagaListener {

    private static final Logger log = LoggerFactory.getLogger(InventorySagaListener.class);

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;

    public InventorySagaListener(ReservationService reservationService, ObjectMapper objectMapper) {
        this.reservationService = reservationService;
        this.objectMapper = objectMapper;
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.ORDER_CREATED, groupId = "inventory-service")
    public void onOrderCreated(String payload) throws Exception {
        var event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        reservationService.handleOrderCreated(event);
    }

    /** Compensation trigger: a charge failed downstream, so the stock held for it must come back. */
    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "inventory-service")
    public void onPaymentFailed(String payload) throws Exception {
        var event = objectMapper.readValue(payload, PaymentFailedEvent.class);
        reservationService.releaseReservationsForOrder(event.orderId());
    }

    @DltHandler
    public void onDlt(String payload) {
        log.error("Message sent to DLT after exhausting retries, payload={}", payload);
    }
}
