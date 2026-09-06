package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.dto.OrderConfirmationRequest;
import com.ecommerce.notification_service.dto.PaymentConfirmationRequest;
import com.ecommerce.notification_service.entity.NotificationLog;
import com.ecommerce.notification_service.entity.NotificationStatus;
import com.ecommerce.notification_service.repository.NotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendOrderConfirmation_success_logsSent() {
        UUID orderId = UUID.randomUUID();
        notificationService.sendOrderConfirmation(
                new OrderConfirmationRequest("buyer@example.com", orderId, new BigDecimal("42.00")));

        verify(emailSender).send(eq("buyer@example.com"), anyString(), contains(orderId.toString()));
        verify(notificationLogRepository).save(argThat(log ->
                log.getRecipient().equals("buyer@example.com") && log.getStatus() == NotificationStatus.SENT));
    }

    @Test
    void sendPaymentConfirmation_emailSenderThrows_logsFailedButDoesNotPropagate() {
        UUID orderId = UUID.randomUUID();
        doThrow(new RuntimeException("smtp down")).when(emailSender).send(anyString(), anyString(), anyString());

        notificationService.sendPaymentConfirmation(
                new PaymentConfirmationRequest("buyer@example.com", orderId, new BigDecimal("99.99")));

        verify(notificationLogRepository).save(argThat((NotificationLog log) -> log.getStatus() == NotificationStatus.FAILED));
    }
}
