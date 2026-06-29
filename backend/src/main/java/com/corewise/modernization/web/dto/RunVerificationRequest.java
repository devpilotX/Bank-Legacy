package com.corewise.modernization.web.dto;

import java.util.List;

/**
 * What we take to run verification for a unit. The engine now runs both the COBOL and
 * the Java itself, so we no longer take any outputs from the caller.
 *
 * <p>Everything here is optional:
 * <ul>
 *   <li>caseIds: run only these confirmed cases. Empty or missing means run them all.
 *   <li>trimTrailingSpace and numericTolerance: the comparison settings. Missing means
 *       use the configured defaults. Whatever is used is recorded with each run.
 * </ul>
 */
public record RunVerificationRequest(
    List<Long> caseIds,
    Boolean trimTrailingSpace,
    Double numericTolerance
) {
}
