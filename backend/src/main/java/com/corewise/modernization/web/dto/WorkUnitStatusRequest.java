package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** What we take to move a work unit to a new status. */
public record WorkUnitStatusRequest(
    @NotBlank(message = "is required") String status
) {
}
