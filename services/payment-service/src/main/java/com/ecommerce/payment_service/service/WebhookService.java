package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.entity.Payment;
import com.ecommerce.payment_service.entity.PaymentStatus;
import com.ecommerce.payment_service.exception.InvalidSignatureException;
import com.ecommerce.payment_service.repository.PaymentRepository;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Handles server-to-server Razorpay webhook events (payment.captured /
 * payment.failed) as a durability net alongside the client-driven verify flow.
 */
@Service
public class WebhookService {

    private final PaymentRepository paymentRepository;
    private final String webhookSecret;

    public WebhookService(PaymentRepository paymentRepository, @Value("${razorpay.webhook-secret}") String webhookSecret) {
        this.paymentRepository = paymentRepository;
        this.webhookSecret = webhookSecret;
    }

    @Transactional
    public void handle(String payload, String signature) {
        boolean valid;
        try {
            valid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (RazorpayException e) {
            throw new InvalidSignatureException("Webhook signature verification failed: " + e.getMessage());
        }
        if (!valid) {
            throw new InvalidSignatureException("Invalid Razorpay webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.optString("event", "");

        JSONObject payloadObj = event.optJSONObject("payload");
        JSONObject paymentWrapper = payloadObj != null ? payloadObj.optJSONObject("payment") : null;
        JSONObject paymentEntity = paymentWrapper != null ? paymentWrapper.optJSONObject("entity") : null;
        if (paymentEntity == null) {
            return;
        }

        String razorpayOrderId = paymentEntity.optString("order_id", null);
        String razorpayPaymentId = paymentEntity.optString("id", null);
        if (razorpayOrderId == null) {
            return;
        }

        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            if ("payment.captured".equals(eventType)) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setRazorpayPaymentId(razorpayPaymentId);
            } else if ("payment.failed".equals(eventType)) {
                payment.setStatus(PaymentStatus.FAILED);
            }
            payment.setUpdatedAt(Instant.now());
        });
    }
}
