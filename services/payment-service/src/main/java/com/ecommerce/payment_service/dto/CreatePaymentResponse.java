package com.ecommerce.payment_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentResponse(
        UUID paymentId,
        String razorpayOrderId,
        String razorpayKeyId,
        BigDecimal amount,
        String currency
) {
}
