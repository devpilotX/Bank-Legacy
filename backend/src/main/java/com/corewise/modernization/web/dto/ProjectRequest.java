package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** What we take to start a new project for a client. */
public record ProjectRequest(
    @NotNull(message = "is required") Long clientId,
    @NotBlank(message = "is required") String name,
    String description
) {
}
