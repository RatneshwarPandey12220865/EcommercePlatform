package com.ecommerce.order_service.exception;

public class AccessDeniedForResourceException extends RuntimeException {
    public AccessDeniedForResourceException(String message) {
        super(message);
    }
}
