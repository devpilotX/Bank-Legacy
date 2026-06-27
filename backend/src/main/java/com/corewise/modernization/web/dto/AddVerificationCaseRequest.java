package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** What we take to add a test case by hand. */
public record AddVerificationCaseRequest(
    @NotBlank(message = "is required") String name,
    String input,
    @NotBlank(message = "is required") String expectedOutput
) {
}
