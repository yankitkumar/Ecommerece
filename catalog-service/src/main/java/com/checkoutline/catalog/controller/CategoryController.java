package com.checkoutline.catalog.controller;

import com.checkoutline.catalog.dto.CategoryRequest;
import com.checkoutline.catalog.model.Category;
import com.checkoutline.catalog.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<Category> list(@RequestParam(required = false) String parentId) {
        return parentId != null
                ? categoryRepository.findByParentId(parentId)
                : categoryRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody CategoryRequest request) {
        return categoryRepository.save(new Category(request.name(), request.parentId()));
    }
}
