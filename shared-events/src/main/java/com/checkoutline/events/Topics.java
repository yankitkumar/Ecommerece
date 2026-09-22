package com.checkoutline.events;

/** Central registry of Kafka topic names so producers and consumers never hand-type strings. */
public final class Topics {

    private Topics() {}

    public static final String ORDER_CREATED = "order.created";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_FAILED = "inventory.failed";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String PRODUCT_UPDATED = "product.updated";
}
