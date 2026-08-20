package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.dto.OrderConfirmationRequest;
import com.ecommerce.notification_service.dto.PaymentConfirmationRequest;
import com.ecommerce.notification_service.entity.NotificationLog;
import com.ecommerce.notification_service.entity.NotificationStatus;
import com.ecommerce.notification_service.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailSender emailSender;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(EmailSender emailSender, NotificationLogRepository notificationLogRepository) {
        this.emailSender = emailSender;
        this.notificationLogRepository = notificationLogRepository;
    }

    public void sendOrderConfirmation(OrderConfirmationRequest request) {
        String subject = "Order confirmed: " + request.orderId();
        String body = "Thanks for your order! Order " + request.orderId()
                + " for $" + request.totalAmount() + " has been received.";
        dispatch(request.userEmail(), subject, body);
    }

    public void sendPaymentConfirmation(PaymentConfirmationRequest request) {
        String subject = "Payment received: " + request.orderId();
        String body = "We received your payment of $" + request.amount()
                + " for order " + request.orderId() + ".";
        dispatch(request.userEmail(), subject, body);
    }

    private void dispatch(String recipient, String subject, String body) {
        NotificationStatus status;
        try {
            emailSender.send(recipient, subject, body);
            status = NotificationStatus.SENT;
        } catch (Exception e) {
            log.warn("Failed to send notification to {}: {}", recipient, e.getMessage());
            status = NotificationStatus.FAILED;
        }
        notificationLogRepository.save(
                NotificationLog.builder()
                        .recipient(recipient)
                        .subject(subject)
                        .body(body)
                        .status(status)
                        .build()
        );
    }
}
