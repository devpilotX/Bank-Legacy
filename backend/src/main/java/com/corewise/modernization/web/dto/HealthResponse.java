package com.corewise.modernization.web.dto;

/**
 * What the /health endpoint sends back: a plain status and the running build
 * version. It is a small record so the JSON shape is obvious and fixed.
 */
public record HealthResponse(String status, String version) {
}
