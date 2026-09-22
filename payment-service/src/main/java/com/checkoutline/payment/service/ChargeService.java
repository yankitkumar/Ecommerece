package com.checkoutline.payment.service;

import com.checkoutline.payment.client.OrderServiceClient;
import com.checkoutline.payment.client.OrderSummary;
import com.checkoutline.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ChargeService {

    private static final Logger log = LoggerFactory.getLogger(ChargeService.class);

    // Mock payment gateway rule, purely so the failure/compensation path is reachable in a
    // demo without integrating a real processor: any order over this amount is "declined".
    private static final BigDecimal MOCK_GATEWAY_LIMIT = new BigDecimal("500.00");

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;
    private final PaymentWriter paymentWriter;

    public ChargeService(PaymentRepository paymentRepository, OrderServiceClient orderServiceClient, PaymentWriter paymentWriter) {
        this.paymentRepository = paymentRepository;
        this.orderServiceClient = orderServiceClient;
        this.paymentWriter = paymentWriter;
    }

    /**
     * Charges for an order once its stock is reserved (triggered by inventory.reserved).
     * Idempotent via the unique order_id constraint on payments: a redelivered event for an
     * order already charged is caught below before it ever reaches the "charge" step.
     */
    public void chargeForOrder(String orderId) {
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.info("Payment for order {} already recorded — skipping duplicate inventory.reserved", orderId);
            return;
        }

        // Sync READ, not an event: Payment needs the amount right now to decide whether to
        // charge, and there's nothing useful to do with "the amount will arrive eventually".
        OrderSummary order = orderServiceClient.getOrder(orderId);
        if (order == null) {
            // Order Service was unreachable (circuit open) — throw so @RetryableTopic retries
            // this message later rather than silently treating an outage as a charge failure.
            throw new IllegalStateException("Could not reach Order Service for order " + orderId);
        }

        if (order.totalAmount().compareTo(MOCK_GATEWAY_LIMIT) > 0) {
            paymentWriter.recordFailure(orderId, "declined: mock gateway limit $" + MOCK_GATEWAY_LIMIT + " exceeded");
            return;
        }

        paymentWriter.recordSuccess(orderId, order.totalAmount());
    }
}
