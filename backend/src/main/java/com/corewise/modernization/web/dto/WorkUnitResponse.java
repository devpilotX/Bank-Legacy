package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.WorkUnit;
import java.time.OffsetDateTime;

/** What we tell the frontend about a work unit, including all three versions of the code. */
public record WorkUnitResponse(
    Long id,
    Long projectId,
    Long sourceFileId,
    String title,
    String originalCode,
    String aiDraftJava,
    String humanJava,
    String status,
    Long ownerId,
    String notes,
    Long createdBy,
    Long approvedBy,
    OffsetDateTime approvedAt,
    OffsetDateTime createdAt
) {
    public static WorkUnitResponse from(WorkUnit unit) {
        return new WorkUnitResponse(unit.getId(), unit.getProjectId(), unit.getSourceFileId(),
            unit.getTitle(), unit.getOriginalCode(), unit.getAiDraftJava(), unit.getHumanJava(),
            unit.getStatus(), unit.getOwnerId(), unit.getNotes(), unit.getCreatedBy(),
            unit.getApprovedBy(), unit.getApprovedAt(), unit.getCreatedAt());
    }
}
