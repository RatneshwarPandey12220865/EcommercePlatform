package com.ecommerce.payment_service.controller;

import com.ecommerce.payment_service.dto.CreatePaymentRequest;
import com.ecommerce.payment_service.dto.CreatePaymentResponse;
import com.ecommerce.payment_service.dto.PaymentResponse;
import com.ecommerce.payment_service.dto.VerifyPaymentRequest;
import com.ecommerce.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(authentication.getName(), request));
    }

    @PostMapping("/verify")
    public PaymentResponse verify(@Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verifyPayment(request);
    }

    @GetMapping("/order/{orderId}")
    public PaymentResponse getByOrderId(@PathVariable UUID orderId) {
        return paymentService.findByOrderId(orderId);
    }
}
