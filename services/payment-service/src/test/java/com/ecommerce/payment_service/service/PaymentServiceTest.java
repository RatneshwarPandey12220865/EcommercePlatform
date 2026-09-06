package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.client.NotificationClient;
import com.ecommerce.payment_service.dto.CreatePaymentRequest;
import com.ecommerce.payment_service.dto.CreatePaymentResponse;
import com.ecommerce.payment_service.dto.VerifyPaymentRequest;
import com.ecommerce.payment_service.entity.Payment;
import com.ecommerce.payment_service.entity.PaymentStatus;
import com.ecommerce.payment_service.exception.InvalidSignatureException;
import com.ecommerce.payment_service.exception.PaymentGatewayException;
import com.ecommerce.payment_service.exception.ResourceNotFoundException;
import com.ecommerce.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String KEY_ID = "rzp_test_key";
    private static final String KEY_SECRET = "test-secret";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private OrderClient orderClient;

    @Mock
    private NotificationClient notificationClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        razorpayClient.orders = orderClient;
        paymentService = new PaymentService(paymentRepository, razorpayClient, notificationClient, KEY_ID, KEY_SECRET);
    }

    @Test
    void createPayment_success_persistsAndReturnsRazorpayDetails() throws RazorpayException {
        UUID orderId = UUID.randomUUID();
        Order razorpayOrder = new Order(new JSONObject().put("id", "order_abc123"));
        when(orderClient.create(any())).thenReturn(razorpayOrder);

        CreatePaymentResponse response = paymentService.createPayment(
                "buyer@example.com", new CreatePaymentRequest(orderId, new BigDecimal("199.00")));

        assertThat(response.razorpayOrderId()).isEqualTo("order_abc123");
        assertThat(response.razorpayKeyId()).isEqualTo(KEY_ID);
        verify(paymentRepository).save(argThat(p ->
                p.getOrderId().equals(orderId) && p.getStatus() == PaymentStatus.CREATED));
    }

    @Test
    void createPayment_gatewayFailure_wrapsAsPaymentGatewayException() throws RazorpayException {
        when(orderClient.create(any())).thenThrow(new RazorpayException("down"));

        assertThatThrownBy(() -> paymentService.createPayment(
                "buyer@example.com", new CreatePaymentRequest(UUID.randomUUID(), new BigDecimal("10.00"))))
                .isInstanceOf(PaymentGatewayException.class);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void verifyPayment_validSignature_marksPaidAndNotifies() throws Exception {
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .orderId(orderId)
                .userEmail("buyer@example.com")
                .amount(new BigDecimal("50.00"))
                .razorpayOrderId("order_xyz")
                .status(PaymentStatus.CREATED)
                .build();
        when(paymentRepository.findByRazorpayOrderId("order_xyz")).thenReturn(Optional.of(payment));

        String signature = hmacSha256Hex("order_xyz|pay_123", KEY_SECRET);
        var response = paymentService.verifyPayment(new VerifyPaymentRequest("order_xyz", "pay_123", signature));

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(payment.getRazorpayPaymentId()).isEqualTo("pay_123");
        verify(notificationClient).sendPaymentConfirmation("buyer@example.com", orderId, new BigDecimal("50.00"));
    }

    @Test
    void verifyPayment_invalidSignature_marksFailedAndThrows() {
        Payment payment = Payment.builder()
                .orderId(UUID.randomUUID())
                .userEmail("buyer@example.com")
                .amount(new BigDecimal("50.00"))
                .razorpayOrderId("order_xyz")
                .status(PaymentStatus.CREATED)
                .build();
        when(paymentRepository.findByRazorpayOrderId("order_xyz")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.verifyPayment(
                new VerifyPaymentRequest("order_xyz", "pay_123", "not-a-real-signature")))
                .isInstanceOf(InvalidSignatureException.class);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(notificationClient, never()).sendPaymentConfirmation(any(), any(), any());
    }

    @Test
    void verifyPayment_unknownRazorpayOrderId_throwsNotFound() {
        when(paymentRepository.findByRazorpayOrderId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.verifyPayment(new VerifyPaymentRequest("missing", "pay_1", "sig")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByOrderId_missing_throwsNotFound() {
        UUID orderId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.findByOrderId(orderId)).isInstanceOf(ResourceNotFoundException.class);
    }

    private static String hmacSha256Hex(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
