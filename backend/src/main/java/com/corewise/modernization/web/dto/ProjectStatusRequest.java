package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** What we take to move a project to a new stage. */
public record ProjectStatusRequest(
    @NotBlank(message = "is required") String status
) {
}
