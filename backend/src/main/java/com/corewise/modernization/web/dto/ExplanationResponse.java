package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.Explanation;
import java.time.OffsetDateTime;

/** What we tell the frontend about an explanation. */
public record ExplanationResponse(
    Long id,
    Long sourceFileId,
    Integer startLine,
    Integer endLine,
    String content,
    String model,
    String status,
    Long createdBy,
    Long approvedBy,
    OffsetDateTime approvedAt,
    OffsetDateTime createdAt
) {
    public static ExplanationResponse from(Explanation e) {
        return new ExplanationResponse(e.getId(), e.getSourceFileId(), e.getStartLine(), e.getEndLine(),
            e.getContent(), e.getModel(), e.getStatus(), e.getCreatedBy(), e.getApprovedBy(),
            e.getApprovedAt(), e.getCreatedAt());
    }
}
