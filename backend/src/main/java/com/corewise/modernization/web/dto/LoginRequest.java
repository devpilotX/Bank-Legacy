package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** What the login endpoint takes: an email and a password. */
public record LoginRequest(
    @NotBlank(message = "is required") @Email(message = "must be a valid email") String email,
    @NotBlank(message = "is required") String password
) {
}
