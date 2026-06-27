package com.corewise.modernization.common;

/** Thrown when the request itself does not make sense. Becomes a 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
