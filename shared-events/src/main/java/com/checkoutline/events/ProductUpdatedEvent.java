package com.checkoutline.events;

import java.math.BigDecimal;

/** Published by Catalog Service on price/name/active changes. Topic is log-compacted by productId. */
public record ProductUpdatedEvent(String productId, String name, BigDecimal price, String currency, boolean active) {}
