package com.ecommerce.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    @Test
    void registerThenLogin_roundTrip() {
        String email = "test-" + UUID.randomUUID() + "@example.com";
        Map<String, String> registerBody = Map.of("email", email, "password", "password123");

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(baseUrl() + "/register", registerBody, Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).containsEntry("email", email).doesNotContainKey("accessToken");
        assertThat(registerResponse.getHeaders().get("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie).startsWith("access_token=").contains("HttpOnly"));

        Map<String, String> loginBody = Map.of("email", email, "password", "password123");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl() + "/login", loginBody, Map.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsEntry("email", email);
    }

    @Test
    void register_duplicateEmail_returnsConflict() {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        Map<String, String> body = Map.of("email", email, "password", "password123");

        restTemplate.postForEntity(baseUrl() + "/register", body, Map.class);
        ResponseEntity<Map> secondAttempt = restTemplate.postForEntity(baseUrl() + "/register", body, Map.class);

        assertThat(secondAttempt.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() {
        String email = "wrongpw-" + UUID.randomUUID() + "@example.com";
        restTemplate.postForEntity(baseUrl() + "/register", Map.of("email", email, "password", "password123"), Map.class);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/login", Map.of("email", email, "password", "wrong-password"), Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
