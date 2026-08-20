package com.ecommerce.order_service.client;

import com.ecommerce.order_service.exception.ProductUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(RestClient.Builder builder, @Value("${services.product-service.url}") String productServiceUrl) {
        this.restClient = builder.baseUrl(productServiceUrl).build();
    }

    public ProductDto getProduct(UUID productId) {
        try {
            ProductDto product = restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductDto.class);
            if (product == null) {
                throw new ProductUnavailableException("Product not found: " + productId);
            }
            return product;
        } catch (RestClientException e) {
            throw new ProductUnavailableException("Could not reach product-service for product " + productId);
        }
    }
}
