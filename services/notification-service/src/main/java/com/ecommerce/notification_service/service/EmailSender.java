package com.ecommerce.notification_service.service;

public interface EmailSender {
    void send(String recipient, String subject, String body);
}
