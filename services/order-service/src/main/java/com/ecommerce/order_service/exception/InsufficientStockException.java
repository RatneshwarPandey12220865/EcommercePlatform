package com.ecommerce.order_service.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for " + productName + ": requested " + requested + ", available " + available);
    }
}
