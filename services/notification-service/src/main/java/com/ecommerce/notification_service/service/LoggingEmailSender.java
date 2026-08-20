package com.ecommerce.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Default sender for local dev when no SMTP credentials are configured -
 * writes the email to the log instead of a real inbox. Swap
 * notification.mode=smtp (with real SMTP env vars) to send for real.
 */
@Service
@ConditionalOnProperty(name = "notification.mode", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String recipient, String subject, String body) {
        log.info("[email:log-mode] to={} subject=\"{}\" body=\"{}\"", recipient, subject, body);
    }
}
