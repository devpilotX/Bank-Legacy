package com.corewise.modernization.common;

/** Thrown when sign-in fails or an account is not allowed to sign in. Becomes a 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
