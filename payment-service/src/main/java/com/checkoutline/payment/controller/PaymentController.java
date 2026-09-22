package com.checkoutline.payment.controller;

import com.checkoutline.payment.model.Payment;
import com.checkoutline.payment.model.PaymentStatus;
import com.checkoutline.payment.repository.PaymentRepository;
import com.checkoutline.payment.service.PaymentWriter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * No route through the gateway on purpose, same as the rest of this service — it's an
 * internal/ops surface, not something a shopper calls directly.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentWriter paymentWriter;

    public PaymentController(PaymentRepository paymentRepository, PaymentWriter paymentWriter) {
        this.paymentRepository = paymentRepository;
        this.paymentWriter = paymentWriter;
    }

    @GetMapping("/{orderId}")
    public Payment get(@PathVariable String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No payment for order " + orderId));
    }

    @PostMapping("/{orderId}/refund")
    public Payment refund(@PathVariable String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No payment for order " + orderId));
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Payment for order " + orderId + " is " + payment.getStatus() + ", not eligible for refund");
        }
        return paymentWriter.recordRefund(payment);
    }
}
