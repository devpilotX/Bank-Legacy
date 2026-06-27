package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.VerificationRun;
import java.time.OffsetDateTime;

/** What we tell the frontend about one run of a case. */
public record VerificationRunResponse(
    Long id,
    Long caseId,
    boolean passed,
    String detail,
    String actualOutput,
    OffsetDateTime createdAt
) {
    public static VerificationRunResponse from(VerificationRun run) {
        return new VerificationRunResponse(run.getId(), run.getCaseId(), run.isPassed(),
            run.getDetail(), run.getActualOutput(), run.getCreatedAt());
    }
}
