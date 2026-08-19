package com.example.fabric.exceptions;

/**
 * Thrown when a create/update operation would violate a uniqueness constraint.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
