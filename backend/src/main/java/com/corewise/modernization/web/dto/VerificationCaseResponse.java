package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.VerificationCase;
import java.time.OffsetDateTime;

/** What we tell the frontend about a test case. */
public record VerificationCaseResponse(
    Long id,
    Long workUnitId,
    String name,
    String input,
    String expectedOutput,
    String origin,
    String status,
    Long createdBy,
    OffsetDateTime createdAt
) {
    public static VerificationCaseResponse from(VerificationCase c) {
        return new VerificationCaseResponse(c.getId(), c.getWorkUnitId(), c.getName(), c.getInput(),
            c.getExpectedOutput(), c.getOrigin(), c.getStatus(), c.getCreatedBy(), c.getCreatedAt());
    }
}
