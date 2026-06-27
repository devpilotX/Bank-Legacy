package com.corewise.modernization.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The one place the rest of the app asks the AI for something.
 *
 * It wraps whichever provider is configured and adds the careful bits: it retries a
 * timeout or a rate limit a few times, backing off a little longer each time, and it
 * gives up with a clear error rather than ever returning a wrong or empty answer. A
 * plain bad request (like a missing key) is not retried, since trying again will not
 * help.
 */
@Service
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);
    private static final long MAX_BACKOFF_MS = 20_000;

    private final AiProvider provider;
    private final AiProperties properties;

    public AiClient(AiProvider provider, AiProperties properties) {
        this.provider = provider;
        this.properties = properties;
    }

    public String complete(String systemPrompt, String userPrompt) {
        int attempts = Math.max(1, properties.maxAttempts());
        long backoffMs = properties.initialBackoff().toMillis();
        AiException lastFailure = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return provider.complete(systemPrompt, userPrompt);
            } catch (AiException e) {
                lastFailure = e;
                if (!e.isRetryable()) {
                    // No point trying again, so surface this clear error now.
                    throw e;
                }
                log.warn("AI call to {} failed on attempt {} of {}: {}",
                    provider.name(), attempt, attempts, e.getMessage());
                if (attempt < attempts) {
                    sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
                }
            }
        }
        throw new AiException(
            "The AI did not respond after several tries. Please try again in a moment.",
            lastFailure, true);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("The AI request was interrupted.", e, false);
        }
    }
}
