package com.checkoutline.notification.listener;

import com.checkoutline.events.OrderCancelledEvent;
import com.checkoutline.events.OrderConfirmedEvent;
import com.checkoutline.events.Topics;
import com.checkoutline.notification.model.Notification;
import com.checkoutline.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * The last consumer in the saga. Nothing ever calls Notification Service directly — it only
 * reacts to events, which is exactly why it being slow or briefly down never affects checkout.
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationListener(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.ORDER_CONFIRMED, groupId = "notification-service")
    public void onOrderConfirmed(String payload) throws Exception {
        var event = objectMapper.readValue(payload, OrderConfirmedEvent.class);
        String body = "Your order " + event.orderId() + " is confirmed and on its way to being packed.";
        send(event.userId(), "ORDER_CONFIRMED", body);
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = Topics.ORDER_CANCELLED, groupId = "notification-service")
    public void onOrderCancelled(String payload) throws Exception {
        var event = objectMapper.readValue(payload, OrderCancelledEvent.class);
        String body = "Your order " + event.orderId() + " could not be completed: " + event.reason();
        send(event.userId(), "ORDER_CANCELLED", body);
    }

    private void send(String userId, String type, String body) {
        // A real implementation would call an email/SMS provider here. Logging + persisting is
        // enough to prove the saga's last hop actually fires, without wiring up SES/Twilio.
        notificationRepository.save(new Notification(userId, type, body));
        log.info("[{}] notified user {}: {}", type, userId, body);
    }

    @DltHandler
    public void onDlt(String payload) {
        log.error("Message sent to DLT after exhausting retries, payload={}", payload);
    }
}
