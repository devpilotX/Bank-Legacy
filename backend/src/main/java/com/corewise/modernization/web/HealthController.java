package com.corewise.modernization.web;

import com.corewise.modernization.service.AppInfoService;
import com.corewise.modernization.web.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple liveness check.
 *
 * Our monitoring, our own scripts, and Cloudflare hit this to confirm the backend
 * is up. It does not touch the database, so it stays fast and keeps answering even
 * if something downstream is having a bad day. It is open to everyone on purpose
 * (see SecurityConfig).
 */
@RestController
public class HealthController {

    private final AppInfoService appInfo;

    public HealthController(AppInfoService appInfo) {
        this.appInfo = appInfo;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", appInfo.version());
    }
}
