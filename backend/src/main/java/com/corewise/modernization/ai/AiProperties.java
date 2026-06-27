package com.corewise.modernization.ai;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How we talk to the AI, all from config. The provider picks Claude or GPT. The
 * keys live only here (filled from environment or the local file), never in code.
 * Timeouts and retries keep a slow or flaky AI from hurting us.
 */
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
    String provider,
    Duration timeout,
    Integer maxAttempts,
    Duration initialBackoff,
    Vendor claude,
    Vendor openai
) {
    public AiProperties {
        if (provider == null || provider.isBlank()) {
            provider = "claude";
        }
        if (timeout == null) {
            timeout = Duration.ofSeconds(30);
        }
        if (maxAttempts == null || maxAttempts < 1) {
            maxAttempts = 3;
        }
        if (initialBackoff == null) {
            initialBackoff = Duration.ofSeconds(1);
        }
        claude = withDefaults(claude, "claude-3-5-sonnet-latest", "https://api.anthropic.com");
        openai = withDefaults(openai, "gpt-4o", "https://api.openai.com");
    }

    private static Vendor withDefaults(Vendor vendor, String model, String baseUrl) {
        if (vendor == null) {
            return new Vendor(null, model, baseUrl);
        }
        return new Vendor(
            vendor.apiKey(),
            (vendor.model() == null || vendor.model().isBlank()) ? model : vendor.model(),
            (vendor.baseUrl() == null || vendor.baseUrl().isBlank()) ? baseUrl : vendor.baseUrl());
    }

    /** The settings for one vendor: its key, which model to use, and its base URL. */
    public record Vendor(String apiKey, String model, String baseUrl) {
    }
}
