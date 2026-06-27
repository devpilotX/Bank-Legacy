package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** What we take to start a new piece of rewrite work. */
public record CreateWorkUnitRequest(
    @NotBlank(message = "is required") String title,
    @NotBlank(message = "is required") String originalCode,
    Long sourceFileId,
    Long ownerId,
    String notes
) {
}
