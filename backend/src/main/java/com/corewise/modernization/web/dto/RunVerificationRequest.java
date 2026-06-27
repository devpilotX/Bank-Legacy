package com.corewise.modernization.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * What we take to run verification for a unit: for each case, the output the new Java
 * actually produced. We compare each one against the case's expected output. (The
 * harness that compiles and runs the Java to produce these outputs is future work.)
 */
public record RunVerificationRequest(
    @NotEmpty(message = "needs at least one result") @Valid List<CaseOutcome> results
) {
    public record CaseOutcome(
        @NotNull(message = "is required") Long caseId,
        String actualOutput
    ) {
    }
}
