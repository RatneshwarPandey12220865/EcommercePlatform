package com.ecommerce.order_service.service;

import com.ecommerce.order_service.client.NotificationClient;
import com.ecommerce.order_service.client.ProductClient;
import com.ecommerce.order_service.client.ProductDto;
import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.dto.OrderItemRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.exception.AccessDeniedForResourceException;
import com.ecommerce.order_service.exception.InsufficientStockException;
import com.ecommerce.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_computesTotalAndPersists() {
        UUID productId = UUID.randomUUID();
        ProductDto product = new ProductDto(productId, "Widget", "desc", new BigDecimal("9.99"), 10);
        when(productClient.getProduct(productId)).thenReturn(product);

        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(productId, 3)));

        OrderResponse response = orderService.createOrder("user@example.com", request);

        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("29.97"));
        assertThat(response.items()).hasSize(1);
        verify(orderRepository).save(any(Order.class));
        verify(notificationClient).sendOrderConfirmation("user@example.com", response.id(), new BigDecimal("29.97"));
    }

    @Test
    void createOrder_insufficientStock_throws() {
        UUID productId = UUID.randomUUID();
        ProductDto product = new ProductDto(productId, "Widget", "desc", new BigDecimal("9.99"), 1);
        when(productClient.getProduct(productId)).thenReturn(product);

        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(productId, 5)));

        assertThatThrownBy(() -> orderService.createOrder("user@example.com", request))
                .isInstanceOf(InsufficientStockException.class);
        verify(orderRepository, never()).save(any());
        verify(notificationClient, never()).sendOrderConfirmation(any(), any(), any());
    }

    @Test
    void findByIdForUser_differentOwner_throwsAccessDenied() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .userEmail("owner@example.com")
                .totalAmount(BigDecimal.TEN)
                .items(List.of())
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.findByIdForUser(orderId, "someone-else@example.com"))
                .isInstanceOf(AccessDeniedForResourceException.class);
    }

    @Test
    void findByIdForUser_owner_returnsOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .userEmail("owner@example.com")
                .totalAmount(BigDecimal.TEN)
                .items(List.<OrderItem>of())
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findByIdForUser(orderId, "owner@example.com");

        assertThat(response.id()).isEqualTo(orderId);
    }
}
