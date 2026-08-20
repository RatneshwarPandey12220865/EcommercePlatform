package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.ProductRequest;
import com.ecommerce.product_service.dto.ProductResponse;
import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.exception.ResourceNotFoundException;
import com.ecommerce.product_service.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public Page<ProductResponse> search(UUID categoryId, String name, Pageable pageable) {
        boolean hasCategory = categoryId != null;
        boolean hasName = StringUtils.hasText(name);

        Page<Product> page;
        if (hasCategory && hasName) {
            page = productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, name, pageable);
        } else if (hasCategory) {
            page = productRepository.findByCategoryId(categoryId, pageable);
        } else if (hasName) {
            page = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }
        return page.map(ProductResponse::from);
    }

    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getEntity(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(request.categoryId() != null ? categoryService.getEntity(request.categoryId()) : null)
                .build();
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getEntity(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        Category category = request.categoryId() != null ? categoryService.getEntity(request.categoryId()) : null;
        product.setCategory(category);
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getEntity(id);
        productRepository.delete(product);
    }

    private Product getEntity(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }
}
