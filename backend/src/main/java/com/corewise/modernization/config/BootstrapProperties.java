package com.corewise.modernization.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The first admin to create when the system has no users yet. Read from config so
 * no credentials live in the code.
 */
@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapProperties(String adminEmail, String adminPassword, String adminName) {

    public BootstrapProperties {
        if (adminName == null || adminName.isBlank()) {
            adminName = "Admin";
        }
    }
}
