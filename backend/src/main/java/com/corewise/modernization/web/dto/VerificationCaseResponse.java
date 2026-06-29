package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.VerificationCase;
import java.time.OffsetDateTime;

/** What we tell the frontend about a test case. A case is its inputs now. */
public record VerificationCaseResponse(
    Long id,
    Long workUnitId,
    String name,
    String input,
    String inputFiles,
    String origin,
    String status,
    Long createdBy,
    OffsetDateTime createdAt
) {
    public static VerificationCaseResponse from(VerificationCase c) {
        return new VerificationCaseResponse(c.getId(), c.getWorkUnitId(), c.getName(), c.getInput(),
            c.getInputFiles(), c.getOrigin(), c.getStatus(), c.getCreatedBy(), c.getCreatedAt());
    }
}
