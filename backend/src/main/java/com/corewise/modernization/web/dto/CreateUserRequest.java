package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** What an admin sends to create a new team member. */
public record CreateUserRequest(
    @NotBlank(message = "is required") @Email(message = "must be a valid email") String email,
    @NotBlank(message = "is required") String fullName,
    @NotBlank(message = "is required")
    @Size(min = 8, message = "must be at least 8 characters") String password,
    @NotBlank(message = "is required")
    @Pattern(regexp = "admin|engineer", message = "must be admin or engineer") String role
) {
}
