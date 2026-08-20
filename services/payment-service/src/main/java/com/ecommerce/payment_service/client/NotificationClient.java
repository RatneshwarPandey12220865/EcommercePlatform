package com.ecommerce.payment_service.client;

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
 * payment processing, so exceptions are swallowed and logged.
 */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;

    public NotificationClient(RestClient.Builder builder, @Value("${services.notification-service.url}") String notificationServiceUrl) {
        this.restClient = builder.baseUrl(notificationServiceUrl).build();
    }

    public void sendPaymentConfirmation(String userEmail, UUID orderId, BigDecimal amount) {
        try {
            restClient.post()
                    .uri("/api/notifications/payment-confirmation")
                    .body(Map.of("userEmail", userEmail, "orderId", orderId, "amount", amount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Failed to notify payment confirmation for order {}: {}", orderId, e.getMessage());
        }
    }
}
