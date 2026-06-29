package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.VerificationRun;
import java.time.OffsetDateTime;

/** What we tell the frontend about one run of a case: the whole picture from the engine. */
public record VerificationRunResponse(
    Long id,
    Long caseId,
    boolean passed,
    String outcome,
    String detail,
    String cobolOutput,
    String javaOutput,
    String diff,
    String differenceKind,
    Boolean normalizedTrailingSpace,
    Double numericTolerance,
    OffsetDateTime createdAt
) {
    public static VerificationRunResponse from(VerificationRun run) {
        return new VerificationRunResponse(run.getId(), run.getCaseId(), run.isPassed(),
            run.getOutcome(), run.getDetail(), run.getCobolOutput(), run.getJavaOutput(),
            run.getDiff(), run.getDifferenceKind(), run.getNormalizedTrailingSpace(),
            run.getNumericTolerance(), run.getCreatedAt());
    }
}
