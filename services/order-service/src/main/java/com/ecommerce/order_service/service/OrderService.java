package com.ecommerce.order_service.service;

import com.ecommerce.order_service.client.ProductClient;
import com.ecommerce.order_service.client.ProductDto;
import com.ecommerce.order_service.dto.CreateOrderRequest;
import com.ecommerce.order_service.dto.OrderItemRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.exception.AccessDeniedForResourceException;
import com.ecommerce.order_service.exception.InsufficientStockException;
import com.ecommerce.order_service.exception.ResourceNotFoundException;
import com.ecommerce.order_service.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    @Transactional
    public OrderResponse createOrder(String userEmail, CreateOrderRequest request) {
        Order order = Order.builder()
                .userEmail(userEmail)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            ProductDto product = productClient.getProduct(itemRequest.productId());
            if (product.stockQuantity() < itemRequest.quantity()) {
                throw new InsufficientStockException(product.name(), itemRequest.quantity(), product.stockQuantity());
            }
            OrderItem item = OrderItem.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .unitPrice(product.price())
                    .quantity(itemRequest.quantity())
                    .build();
            order.addItem(item);
            total = total.add(product.price().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }
        order.setTotalAmount(total);

        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    public Page<OrderResponse> findForUser(String userEmail, Pageable pageable) {
        return orderRepository.findByUserEmail(userEmail, pageable).map(OrderResponse::from);
    }

    public OrderResponse findByIdForUser(UUID id, String userEmail) {
        Order order = getEntity(id);
        if (!order.getUserEmail().equals(userEmail)) {
            throw new AccessDeniedForResourceException("Order does not belong to this user");
        }
        return OrderResponse.from(order);
    }

    private Order getEntity(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }
}
