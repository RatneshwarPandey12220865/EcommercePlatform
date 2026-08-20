package com.ecommerce.order_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Best-effort call to notification-service. A failure here must never fail
 * order creation, so exceptions are swallowed and logged.
 */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;

    public NotificationClient(RestClient.Builder builder, @Value("${services.notification-service.url}") String notificationServiceUrl) {
        this.restClient = builder.baseUrl(notificationServiceUrl).build();
    }

    public void sendOrderConfirmation(String userEmail, UUID orderId, BigDecimal totalAmount) {
        try {
            restClient.post()
                    .uri("/api/notifications/order-confirmation")
                    .body(Map.of("userEmail", userEmail, "orderId", orderId, "totalAmount", totalAmount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Failed to notify order confirmation for order {}: {}", orderId, e.getMessage());
        }
    }
}
