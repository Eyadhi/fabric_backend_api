package com.example.fabric.exceptions;

/**
 * Thrown when a business rule is violated (e.g., product not running on machine,
 * invalid analytics period, shift overlap).
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
