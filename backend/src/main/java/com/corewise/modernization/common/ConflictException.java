package com.corewise.modernization.common;

/** Thrown when a request clashes with what already exists, like a duplicate email. Becomes a 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
