package com.checkoutline.catalog.service;

import com.checkoutline.catalog.dto.ProductRequest;
import com.checkoutline.catalog.model.Product;
import com.checkoutline.catalog.repository.ProductRepository;
import com.checkoutline.events.ProductUpdatedEvent;
import com.checkoutline.events.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

/**
 * MongoDB has no shared transaction with Kafka, so this is a direct publish rather than the
 * transactional-outbox pattern the Postgres-backed services use — an acceptable simplification
 * here because product.updated is a best-effort cache-invalidation signal (Cart, Search), not
 * money moving. A stricter deployment could still add MongoDB change-stream-based CDC instead.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Product create(ProductRequest request) {
        Product product = new Product(request.sku(), request.name(), request.description(), request.price(),
                request.currency(), request.categoryId(), request.images(), request.attributes());
        product = productRepository.save(product);
        publish(product);
        return product;
    }

    public Product updatePrice(String productId, BigDecimal newPrice) {
        Product product = get(productId);
        product.updatePrice(newPrice);
        product = productRepository.save(product);
        publish(product);
        return product;
    }

    public Product get(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No product " + productId));
    }

    private void publish(Product product) {
        var event = new ProductUpdatedEvent(product.getId(), product.getName(), product.getPrice(), product.getCurrency(), product.isActive());
        try {
            // Keyed by productId so log compaction (cleanup.policy=compact) can keep just the
            // latest update per product — see the design doc's "topics & retention" section.
            kafkaTemplate.send(Topics.PRODUCT_UPDATED, product.getId(), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to publish product.updated for {} — Cart/Search may serve a stale price until the next update", product.getId(), e);
        }
    }
}
