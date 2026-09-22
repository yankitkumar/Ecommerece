package com.checkoutline.catalog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name;
    private String parentId;

    protected Category() {
        // Spring Data
    }

    public Category(String name, String parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getParentId() { return parentId; }
}
