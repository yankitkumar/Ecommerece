package com.checkoutline.events;

/** Published by Inventory Service when one or more line items could not be reserved (out of stock). */
public record InventoryFailedEvent(String orderId, String reason) {}
