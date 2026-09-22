package com.checkoutline.payment.listener;

import com.checkoutline.events.InventoryReservedEvent;
import com.checkoutline.events.Topics;
import com.checkoutline.payment.service.ChargeService;
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
public class PaymentSagaListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaListener.class);

    private final ChargeService chargeService;
    private final ObjectMapper objectMapper;

    public PaymentSagaListener(ChargeService chargeService, ObjectMapper objectMapper) {
        this.chargeService = chargeService;
        this.objectMapper = objectMapper;
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.INVENTORY_RESERVED, groupId = "payment-service")
    public void onInventoryReserved(String payload) throws Exception {
        var event = objectMapper.readValue(payload, InventoryReservedEvent.class);
        chargeService.chargeForOrder(event.orderId());
    }

    @DltHandler
    public void onDlt(String payload) {
        log.error("Message sent to DLT after exhausting retries, payload={}", payload);
    }
}
