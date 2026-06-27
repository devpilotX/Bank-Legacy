package com.corewise.modernization.ai;

/**
 * Something went wrong talking to the AI. We carry whether it is worth retrying
 * (a timeout or a rate limit) or not (a bad request or a missing key), so the
 * wrapper knows when to try again. The message is always plain enough to show a
 * person.
 */
public class AiException extends RuntimeException {

    private final boolean retryable;

    public AiException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public AiException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
