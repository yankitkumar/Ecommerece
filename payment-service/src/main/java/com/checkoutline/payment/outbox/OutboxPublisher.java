package com.checkoutline.payment.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
            event.markPublished();
            log.debug("Published outbox event {} ({}) for aggregate {}", event.getId(), event.getEventType(), event.getAggregateId());
        }
    }
}
