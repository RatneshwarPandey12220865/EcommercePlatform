package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.ProductRequest;
import com.ecommerce.product_service.dto.ProductResponse;
import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.exception.ResourceNotFoundException;
import com.ecommerce.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    @Test
    void search_withNoFilters_delegatesToFindAll() {
        Pageable pageable = Pageable.unpaged();
        Product product = Product.builder().id(UUID.randomUUID()).name("Widget").price(BigDecimal.TEN).stockQuantity(5).build();
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = productService.search(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Widget");
        verify(productRepository).findAll(pageable);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void search_withCategoryAndName_usesCombinedQuery() {
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        when(productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, "widget", pageable))
                .thenReturn(new PageImpl<>(List.of()));

        productService.search(categoryId, "widget", pageable);

        verify(productRepository).findByCategoryIdAndNameContainingIgnoreCase(categoryId, "widget", pageable);
    }

    @Test
    void findById_missingProduct_throws() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withCategoryId_looksUpCategoryAndSaves() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Gadgets").build();
        when(categoryService.getEntity(categoryId)).thenReturn(category);

        ProductRequest request = new ProductRequest("Widget", "desc", new BigDecimal("19.99"), 10, categoryId);

        ProductResponse response = productService.create(request);

        assertThat(response.categoryName()).isEqualTo("Gadgets");
        verify(productRepository).save(argThat(p -> p.getName().equals("Widget") && p.getCategory() == category));
    }

    @Test
    void delete_missingProduct_throwsAndNeverDeletes() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id)).isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).delete(any());
    }
}
