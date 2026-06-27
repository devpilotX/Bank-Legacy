package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** What we take to edit a project's name or description. The client never changes. */
public record ProjectUpdateRequest(
    @NotBlank(message = "is required") String name,
    String description
) {
}
