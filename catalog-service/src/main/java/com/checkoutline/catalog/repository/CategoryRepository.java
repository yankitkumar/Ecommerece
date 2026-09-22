package com.checkoutline.catalog.repository;

import com.checkoutline.catalog.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByParentId(String parentId);
}
