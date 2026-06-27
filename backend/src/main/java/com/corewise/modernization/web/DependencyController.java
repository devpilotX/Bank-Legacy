package com.corewise.modernization.web;

import com.corewise.modernization.service.DependencyMapService;
import com.corewise.modernization.web.dto.DependencyEdgeResponse;
import com.corewise.modernization.web.dto.DependencyMapResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The dependency map for a project. Building it parses the code for obvious links and,
 * if asked, lets the AI suggest trickier ones for a person to confirm. The AI part is
 * off by default so a build never depends on the AI or runs up its cost unasked.
 */
@RestController
public class DependencyController {

    private final DependencyMapService dependencyMap;

    public DependencyController(DependencyMapService dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    @PostMapping("/api/projects/{projectId}/dependency-map")
    public DependencyMapResponse build(@PathVariable Long projectId,
                                       @RequestParam(name = "ai", defaultValue = "false") boolean includeAi) {
        return DependencyMapResponse.from(dependencyMap.buildOrRefresh(projectId, includeAi));
    }

    @GetMapping("/api/projects/{projectId}/dependency-map")
    public DependencyMapResponse get(@PathVariable Long projectId) {
        return DependencyMapResponse.from(dependencyMap.getMap(projectId));
    }

    @PostMapping("/api/dependency-links/{edgeId}/confirm")
    public DependencyEdgeResponse confirm(@PathVariable Long edgeId) {
        return DependencyEdgeResponse.from(dependencyMap.confirmLink(edgeId));
    }

    @PostMapping("/api/dependency-links/{edgeId}/reject")
    public DependencyEdgeResponse reject(@PathVariable Long edgeId) {
        return DependencyEdgeResponse.from(dependencyMap.rejectLink(edgeId));
    }
}
