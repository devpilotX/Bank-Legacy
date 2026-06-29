package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * What we take to add a test case by hand. A case is just inputs now. The expected
 * output is no longer typed in; the engine gets it by running the original COBOL.
 * inputFiles is an optional JSON object of file name to content, for programs that read
 * files instead of standard input.
 */
public record AddVerificationCaseRequest(
    @NotBlank(message = "is required") String name,
    String input,
    String inputFiles
) {
}
