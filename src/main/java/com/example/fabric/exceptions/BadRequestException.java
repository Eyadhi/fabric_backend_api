package com.example.fabric.exceptions;

/**
 * Thrown when the client sends invalid or incomplete input.
 * Maps to HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
