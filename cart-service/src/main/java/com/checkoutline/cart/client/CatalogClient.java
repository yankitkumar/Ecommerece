package com.checkoutline.cart.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cart Service asks Catalog for the CURRENT price at add-to-cart time — a read the caller
 * needs an immediate answer for, so it's a synchronous call, not an event (see the design
 * doc's "queries are sync, state changes are async" rule).
 */
@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(@Value("${services.catalog.base-url}") String catalogBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(catalogBaseUrl).build();
    }

    public ProductSummary getProduct(String productId) {
        return restClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .body(ProductSummary.class);
    }
}
