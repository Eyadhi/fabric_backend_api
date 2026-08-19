package com.example.fabric.exceptions;

/**
 * Thrown when an uploaded file cannot be read, parsed, or stored.
 * Maps to HTTP 500 Internal Server Error.
 */
public class FileProcessingException extends RuntimeException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
