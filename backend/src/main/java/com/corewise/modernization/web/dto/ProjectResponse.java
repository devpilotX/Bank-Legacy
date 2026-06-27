package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.Project;
import java.time.OffsetDateTime;

/** What we tell the frontend about a project. */
public record ProjectResponse(
    Long id,
    Long clientId,
    String name,
    String description,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getClientId(), project.getName(),
            project.getDescription(), project.getStatus(), project.getCreatedAt(), project.getUpdatedAt());
    }
}
