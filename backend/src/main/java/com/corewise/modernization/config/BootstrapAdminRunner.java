package com.corewise.modernization.config;

import com.corewise.modernization.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the very first admin account on startup, but only when the users table
 * is empty and an admin email and password are set in config. After that first
 * account exists, this does nothing, so it is safe to leave on.
 */
@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final UserService users;
    private final BootstrapProperties properties;

    public BootstrapAdminRunner(UserService users, BootstrapProperties properties) {
        this.users = users;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        if (isBlank(properties.adminEmail()) || isBlank(properties.adminPassword())) {
            log.warn("No users exist yet and no bootstrap admin is set. Set app.bootstrap.admin-email "
                + "and app.bootstrap.admin-password, then restart, to create the first admin.");
            return;
        }
        users.create(properties.adminEmail(), properties.adminName(),
            properties.adminPassword(), UserService.ROLE_ADMIN);
        log.info("Created the first admin account for {}.", properties.adminEmail());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
