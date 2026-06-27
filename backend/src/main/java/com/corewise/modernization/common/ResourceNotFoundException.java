package com.corewise.modernization.common;

/** Thrown when something the caller asked for is not there. Becomes a 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
