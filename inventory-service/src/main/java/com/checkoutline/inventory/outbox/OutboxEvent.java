package com.checkoutline.inventory.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** Same transactional-outbox pattern as Order Service — see that module's OutboxEvent for the rationale. */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected OutboxEvent() {
        // JPA
    }

    public OutboxEvent(String aggregateId, String eventType, String payload) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public void markPublished() { this.published = true; }

    public String getId() { return id; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public boolean isPublished() { return published; }
    public Instant getCreatedAt() { return createdAt; }
}
