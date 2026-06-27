package com.corewise.modernization.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the sign-in tokens, read from config. The secret is required and
 * lives only in config (local file or environment), never in the code.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, String issuer, Duration expiry) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "corewise-modernization";
        }
        if (expiry == null) {
            expiry = Duration.ofHours(8);
        }
    }
}
