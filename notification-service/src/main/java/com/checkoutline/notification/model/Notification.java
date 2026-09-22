package com.checkoutline.notification.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, length = 40)
    private String type; // ORDER_CONFIRMED, ORDER_CANCELLED, ...

    @Column(nullable = false, length = 20)
    private String channel = "EMAIL";

    @Column(nullable = false, length = 20)
    private String status = "SENT";

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt = Instant.now();

    protected Notification() {
        // JPA
    }

    public Notification(String userId, String type, String body) {
        this.userId = userId;
        this.type = type;
        this.body = body;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getChannel() { return channel; }
    public String getStatus() { return status; }
    public String getBody() { return body; }
    public Instant getSentAt() { return sentAt; }
}
