package com.checkoutline.order.outbox;

import com.checkoutline.order.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polls the outbox table and republishes anything not yet on Kafka. This is what closes the
 * gap between "the order committed" and "the event actually left the building" — the app
 * never calls Kafka directly from the request thread, so a Kafka hiccup can't fail the write.
 * (A production system would usually swap this poller for Debezium reading the WAL via CDC.)
 */
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
            // Key by aggregateId (orderId) so every event for the same order lands on the
            // same partition and is processed in order by any consumer.
            kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
            event.markPublished();
            log.debug("Published outbox event {} ({}) for aggregate {}", event.getId(), event.getEventType(), event.getAggregateId());
        }
    }
}
