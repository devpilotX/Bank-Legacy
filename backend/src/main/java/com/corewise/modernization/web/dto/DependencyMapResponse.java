package com.corewise.modernization.web.dto;

import com.corewise.modernization.service.DependencyMapService;
import java.util.List;

/** The whole map: parts and links, ready for the frontend to draw. */
public record DependencyMapResponse(
    List<DependencyNodeResponse> nodes,
    List<DependencyEdgeResponse> edges
) {
    public static DependencyMapResponse from(DependencyMapService.DependencyMap map) {
        return new DependencyMapResponse(
            map.nodes().stream().map(DependencyNodeResponse::from).toList(),
            map.edges().stream().map(DependencyEdgeResponse::from).toList());
    }
}
