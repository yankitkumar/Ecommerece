package com.checkoutline.catalog.controller;

import com.checkoutline.catalog.dto.PriceUpdateRequest;
import com.checkoutline.catalog.dto.ProductRequest;
import com.checkoutline.catalog.model.Product;
import com.checkoutline.catalog.repository.ProductRepository;
import com.checkoutline.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String categoryId) {
        return categoryId != null
                ? productRepository.findByCategoryIdAndActiveTrue(categoryId)
                : productRepository.findByActiveTrue();
    }

    @GetMapping("/{productId}")
    public Product get(@PathVariable String productId) {
        return productService.get(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    /** PATCH: only the price changes, not the whole product — see the API design section. */
    @PatchMapping("/{productId}/price")
    public Product updatePrice(@PathVariable String productId, @Valid @RequestBody PriceUpdateRequest request) {
        return productService.updatePrice(productId, request.price());
    }
}
