package com.corewise.modernization.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Tells the rest of the app basic facts about this build, like its version.
 *
 * The version is set by Maven at build time (see app.version in
 * application.properties). If that value is ever missing, we report "unknown"
 * instead of leaking a raw placeholder string.
 */
@Service
public class AppInfoService {

    private final String version;

    public AppInfoService(@Value("${app.version:unknown}") String version) {
        this.version = version;
    }

    public String version() {
        return version;
    }
}
