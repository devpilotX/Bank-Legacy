package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.DependencyNode;

/** A part of the old system, for the map. */
public record DependencyNodeResponse(Long id, Long projectId, String name, String kind, Long sourceFileId) {
    public static DependencyNodeResponse from(DependencyNode node) {
        return new DependencyNodeResponse(node.getId(), node.getProjectId(), node.getName(),
            node.getKind(), node.getSourceFileId());
    }
}
