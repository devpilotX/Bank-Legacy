package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.DependencyEdge;

/** A link between two parts. origin and status tell the frontend which links are AI
 * suggestions waiting on a human. */
public record DependencyEdgeResponse(
    Long id,
    Long projectId,
    Long fromNodeId,
    Long toNodeId,
    String kind,
    String origin,
    String status,
    String detail
) {
    public static DependencyEdgeResponse from(DependencyEdge edge) {
        return new DependencyEdgeResponse(edge.getId(), edge.getProjectId(), edge.getFromNodeId(),
            edge.getToNodeId(), edge.getKind(), edge.getOrigin(), edge.getStatus(), edge.getDetail());
    }
}
