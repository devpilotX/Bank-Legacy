package com.corewise.modernization.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where uploaded source files are kept on disk. The root can be any folder; in
 * production it should be a real data volume that gets backed up.
 */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String root) {

    public StorageProperties {
        if (root == null || root.isBlank()) {
            root = "var/storage";
        }
    }
}
