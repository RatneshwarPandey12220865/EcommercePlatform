package com.ecommerce.payment_service.dto;

import com.ecommerce.payment_service.entity.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        String status,
        BigDecimal amount,
        String currency
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getStatus().name(), p.getAmount(), p.getCurrency());
    }
}
