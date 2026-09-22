package com.checkoutline.inventory.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Releases stock reservations that never got a payment.completed/failed — e.g. Payment Service
 * was down, or a message was lost. Without this, a stuck RESERVED row holds stock forever.
 */
@Component
public class ReservationExpiryJob {

    private final ReservationService reservationService;
    private final Duration expiryAfter;

    public ReservationExpiryJob(ReservationService reservationService,
                                 @Value("${inventory.reservation.expiry-minutes:15}") long expiryMinutes) {
        this.reservationService = reservationService;
        this.expiryAfter = Duration.ofMinutes(expiryMinutes);
    }

    @Scheduled(fixedDelayString = "${inventory.reservation.expiry-check-interval-ms:60000}")
    public void expireStaleReservations() {
        reservationService.expireStaleReservations(Instant.now().minus(expiryAfter));
    }
}
