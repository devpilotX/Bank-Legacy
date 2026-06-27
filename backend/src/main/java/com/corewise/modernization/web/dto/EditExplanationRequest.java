package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** What we take when an engineer edits an explanation. */
public record EditExplanationRequest(
    @NotBlank(message = "is required") String content
) {
}
