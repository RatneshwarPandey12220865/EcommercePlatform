package com.ecommerce.product_service.dto;

import com.ecommerce.product_service.entity.Category;

import java.util.UUID;

public record CategoryResponse(UUID id, String name) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }
}
