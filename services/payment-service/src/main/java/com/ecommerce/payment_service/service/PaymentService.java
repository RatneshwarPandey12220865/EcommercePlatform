package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.dto.CreatePaymentRequest;
import com.ecommerce.payment_service.dto.CreatePaymentResponse;
import com.ecommerce.payment_service.dto.PaymentResponse;
import com.ecommerce.payment_service.dto.VerifyPaymentRequest;
import com.ecommerce.payment_service.entity.Payment;
import com.ecommerce.payment_service.entity.PaymentStatus;
import com.ecommerce.payment_service.exception.InvalidSignatureException;
import com.ecommerce.payment_service.exception.PaymentGatewayException;
import com.ecommerce.payment_service.exception.ResourceNotFoundException;
import com.ecommerce.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            RazorpayClient razorpayClient,
            @Value("${razorpay.key-id}") String razorpayKeyId,
            @Value("${razorpay.key-secret}") String razorpayKeySecret
    ) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
    }

    @Transactional
    public CreatePaymentResponse createPayment(String userEmail, CreatePaymentRequest request) {
        // Razorpay amounts are in the smallest currency unit (paise for INR).
        int amountInPaise = request.amount().movePointRight(2).intValueExact();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", request.orderId().toString());

        Order razorpayOrder;
        try {
            razorpayOrder = razorpayClient.orders.create(orderRequest);
        } catch (RazorpayException e) {
            throw new PaymentGatewayException("Failed to create Razorpay order", e);
        }

        String razorpayOrderId = razorpayOrder.get("id");

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .userEmail(userEmail)
                .amount(request.amount())
                .currency("INR")
                .status(PaymentStatus.CREATED)
                .razorpayOrderId(razorpayOrderId)
                .build();
        paymentRepository.save(payment);

        return new CreatePaymentResponse(payment.getId(), razorpayOrderId, razorpayKeyId, payment.getAmount(), payment.getCurrency());
    }

    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order " + request.razorpayOrderId()));

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", request.razorpayOrderId());
        options.put("razorpay_payment_id", request.razorpayPaymentId());
        options.put("razorpay_signature", request.razorpaySignature());

        boolean valid;
        try {
            valid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (RazorpayException e) {
            throw new InvalidSignatureException("Signature verification failed: " + e.getMessage());
        }

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(Instant.now());
            throw new InvalidSignatureException("Invalid Razorpay payment signature");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setUpdatedAt(Instant.now());
        return PaymentResponse.from(payment);
    }

    public PaymentResponse findByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment for order " + orderId));
        return PaymentResponse.from(payment);
    }
}
