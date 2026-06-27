package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** What we take to create or update a client. Status and notes are optional. */
public record ClientRequest(
    @NotBlank(message = "is required") String name,
    String status,
    String notes
) {
}
