package com.corewise.modernization.web.dto;

import java.time.Instant;

/** What the login endpoint returns: the token, when it expires, and who you are. */
public record LoginResponse(String token, Instant expiresAt, UserResponse user) {
}
