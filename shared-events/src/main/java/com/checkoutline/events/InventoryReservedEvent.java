package com.checkoutline.events;

/** Published by Inventory Service once every line item's stock has been reserved for this order. */
public record InventoryReservedEvent(String orderId) {}
