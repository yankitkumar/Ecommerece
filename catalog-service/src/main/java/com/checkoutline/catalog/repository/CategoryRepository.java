package com.checkoutline.catalog.repository;

import com.checkoutline.catalog.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
