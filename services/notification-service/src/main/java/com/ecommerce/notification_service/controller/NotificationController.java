package com.ecommerce.notification_service.controller;

import com.ecommerce.notification_service.dto.OrderConfirmationRequest;
import com.ecommerce.notification_service.dto.PaymentConfirmationRequest;
import com.ecommerce.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/order-confirmation")
    public ResponseEntity<Void> orderConfirmation(@Valid @RequestBody OrderConfirmationRequest request) {
        notificationService.sendOrderConfirmation(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/payment-confirmation")
    public ResponseEntity<Void> paymentConfirmation(@Valid @RequestBody PaymentConfirmationRequest request) {
        notificationService.sendPaymentConfirmation(request);
        return ResponseEntity.accepted().build();
    }
}
