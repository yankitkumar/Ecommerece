package com.checkoutline.payment.service;

import com.checkoutline.events.PaymentCompletedEvent;
import com.checkoutline.events.PaymentFailedEvent;
import com.checkoutline.events.Topics;
import com.checkoutline.payment.model.Payment;
import com.checkoutline.payment.model.PaymentStatus;
import com.checkoutline.payment.outbox.OutboxEvent;
import com.checkoutline.payment.outbox.OutboxEventRepository;
import com.checkoutline.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Split out from ChargeService so @Transactional actually applies: Spring's proxy-based AOP
 * doesn't intercept a method calling another method on `this` in the same class, so these
 * writes need to live on a separate bean that ChargeService calls through the Spring proxy.
 */
@Component
public class PaymentWriter {

    private static final Logger log = LoggerFactory.getLogger(PaymentWriter.class);

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public PaymentWriter(PaymentRepository paymentRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordSuccess(String orderId, BigDecimal amount) {
        String providerRef = "mock_" + UUID.randomUUID();
        Payment payment = paymentRepository.save(new Payment(orderId, amount, PaymentStatus.SUCCESS, providerRef));
        writeOutbox(orderId, Topics.PAYMENT_COMPLETED, new PaymentCompletedEvent(orderId, payment.getId(), amount));
        log.info("Charged order {} for {} (providerRef={})", orderId, amount, providerRef);
    }

    @Transactional
    public void recordFailure(String orderId, String reason) {
        paymentRepository.save(new Payment(orderId, BigDecimal.ZERO, PaymentStatus.FAILED, null));
        writeOutbox(orderId, Topics.PAYMENT_FAILED, new PaymentFailedEvent(orderId, reason));
        log.warn("Payment failed for order {}: {}", orderId, reason);
    }

    /** Mock refund: marks a successful charge as REFUNDED. No real gateway call, no event — nothing downstream reacts to a refund today. */
    @Transactional
    public Payment recordRefund(Payment payment) {
        payment.markRefunded();
        Payment saved = paymentRepository.save(payment);
        log.info("Refunded order {} (payment {})", payment.getOrderId(), payment.getId());
        return saved;
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
