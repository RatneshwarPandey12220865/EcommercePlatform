package com.ecommerce.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentConfirmationRequest(
        @NotBlank @Email String userEmail,
        @NotNull UUID orderId,
        @NotNull BigDecimal amount
) {
}
