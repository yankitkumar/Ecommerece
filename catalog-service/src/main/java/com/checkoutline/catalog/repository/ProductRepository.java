package com.checkoutline.catalog.repository;

import com.checkoutline.catalog.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategoryIdAndActiveTrue(String categoryId);
    List<Product> findByActiveTrue();
}
