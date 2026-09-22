package com.checkoutline.payment.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Payment needs to know the order's amount before it can charge — a READ, so it's a plain
 * synchronous call, not a Kafka event (see the design doc's "queries are sync" rule). The
 * circuit breaker means an Order Service outage degrades this one call instead of jamming
 * every message this consumer will ever process again.
 */
@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(@Value("${services.order.base-url}") String orderServiceBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(orderServiceBaseUrl).build();
    }

    @CircuitBreaker(name = "order-service", fallbackMethod = "fallbackOrderSummary")
    public OrderSummary getOrder(String orderId) {
        return restClient.get()
                .uri("/orders/{id}", orderId)
                .retrieve()
                .body(OrderSummary.class);
    }

    /**
     * Fallback when Order Service is down or the breaker is open: null amount, causes the
     * caller to treat the charge as un-processable this attempt — @RetryableTopic backs off
     * and tries again, by which point Order Service has hopefully recovered.
     */
    private OrderSummary fallbackOrderSummary(String orderId, Throwable t) {
        return null;
    }
}
