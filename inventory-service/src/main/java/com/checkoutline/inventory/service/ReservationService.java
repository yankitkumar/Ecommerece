package com.checkoutline.inventory.service;

import com.checkoutline.events.InventoryFailedEvent;
import com.checkoutline.events.InventoryReservedEvent;
import com.checkoutline.events.OrderCreatedEvent;
import com.checkoutline.events.OrderItemPayload;
import com.checkoutline.events.Topics;
import com.checkoutline.inventory.model.Inventory;
import com.checkoutline.inventory.model.ReservationStatus;
import com.checkoutline.inventory.model.StockReservation;
import com.checkoutline.inventory.outbox.OutboxEvent;
import com.checkoutline.inventory.outbox.OutboxEventRepository;
import com.checkoutline.inventory.repository.InventoryRepository;
import com.checkoutline.inventory.repository.StockReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ReservationService(InventoryRepository inventoryRepository,
                               StockReservationRepository reservationRepository,
                               OutboxEventRepository outboxEventRepository,
                               ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Reserves stock for every line item of the order in one local transaction.
     * All-or-nothing, done as check-then-mutate rather than mutate-then-rollback: first every
     * item is confirmed available, and only then are any rows actually decremented — so a
     * short item never leaves a partial reservation for this method to unwind. (A rare
     * concurrent race between the check and the mutation is still caught by Inventory's
     * @Version optimistic lock, which fails the transaction outright for a retry/DLT to handle.)
     *
     * Idempotent: if a reservation already exists for this order, the event is a
     * redelivery and is skipped rather than double-reserving stock.
     */
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        if (reservationRepository.existsByOrderId(event.orderId())) {
            log.info("Reservation for order {} already exists — skipping duplicate order.created", event.orderId());
            return;
        }

        for (OrderItemPayload item : event.items()) {
            Inventory inventory = inventoryRepository.findById(item.productId()).orElse(null);
            if (inventory == null || inventory.getAvailableQty() < item.quantity()) {
                log.warn("Cannot reserve {} units of {} for order {} — insufficient stock",
                        item.quantity(), item.productId(), event.orderId());
                writeOutbox(event.orderId(), Topics.INVENTORY_FAILED,
                        new InventoryFailedEvent(event.orderId(), "product " + item.productId() + " has insufficient stock"));
                return; // nothing was mutated yet — no reservation to unwind
            }
        }

        for (OrderItemPayload item : event.items()) {
            Inventory inventory = inventoryRepository.findById(item.productId()).orElseThrow();
            inventory.reserve(item.quantity());
            inventoryRepository.save(inventory);
            reservationRepository.save(new StockReservation(event.orderId(), item.productId(), item.quantity(), ReservationStatus.RESERVED));
        }

        writeOutbox(event.orderId(), Topics.INVENTORY_RESERVED, new InventoryReservedEvent(event.orderId()));
        log.info("Reserved stock for order {} ({} line items)", event.orderId(), event.items().size());
    }

    /** Compensation: releases every reservation held for this order, e.g. after payment.failed. */
    @Transactional
    public void releaseReservationsForOrder(String orderId) {
        var reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            log.info("No reservations to release for order {} (already released, or never reserved)", orderId);
            return;
        }
        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.RESERVED) {
                continue; // already released — idempotent under redelivery
            }
            Inventory inventory = inventoryRepository.findById(reservation.getProductId()).orElseThrow();
            inventory.release(reservation.getQuantity());
            inventoryRepository.save(inventory);
            reservation.markReleased();
            reservationRepository.save(reservation);
        }
        log.info("Released {} reservation(s) for order {}", reservations.size(), orderId);
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
