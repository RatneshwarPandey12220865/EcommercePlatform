package com.ecommerce.order_service;

import com.ecommerce.order_service.client.NotificationClient;
import com.ecommerce.order_service.client.ProductClient;
import com.ecommerce.order_service.client.ProductDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Matches the default jwt.secret in application.properties (JWT_SECRET env override
    // is not set in tests), so tokens minted here validate the same way auth-service's would.
    private static final String JWT_SECRET = "dev-only-change-me-dev-only-change-me-32bytes";

    // order-service's real job here is orchestration + persistence; product-service
    // and notification-service are external systems and are stubbed for this test.
    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private NotificationClient notificationClient;

    private String userEmail;
    private String accessToken;

    @BeforeEach
    void setUp() {
        userEmail = "buyer-" + UUID.randomUUID() + "@example.com";
        accessToken = Jwts.builder()
                .subject(userEmail)
                .claim("role", "USER")
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/orders";
    }

    private HttpEntity<Map<String, Object>> authedBody(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void createOrder_persistsAndReturnsOrder() {
        UUID productId = UUID.randomUUID();
        when(productClient.getProduct(any())).thenReturn(new ProductDto(productId, "Widget", "desc", new BigDecimal("19.99"), 5));

        Map<String, Object> body = Map.of("items", List.of(Map.of("productId", productId, "quantity", 2)));

        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl(), authedBody(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("status", "PENDING");
    }

    @Test
    void listOrders_forAuthenticatedUser_returnsOwnOrders() {
        UUID productId = UUID.randomUUID();
        when(productClient.getProduct(any())).thenReturn(new ProductDto(productId, "Widget", "desc", new BigDecimal("5.00"), 5));
        restTemplate.postForEntity(baseUrl(), authedBody(Map.of("items", List.of(Map.of("productId", productId, "quantity", 1)))), Map.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl(), org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody().get("content")).isNotEmpty();
    }

    @Test
    void createOrder_withoutAuth_returnsUnauthorized() {
        Map<String, Object> body = Map.of("items", List.of(Map.of("productId", UUID.randomUUID(), "quantity", 1)));
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl(), body, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
