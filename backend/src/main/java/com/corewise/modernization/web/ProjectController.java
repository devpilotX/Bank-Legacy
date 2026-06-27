package com.corewise.modernization.web;

import com.corewise.modernization.domain.model.Project;
import com.corewise.modernization.service.ProjectService;
import com.corewise.modernization.web.dto.ProjectRequest;
import com.corewise.modernization.web.dto.ProjectResponse;
import com.corewise.modernization.web.dto.ProjectStatusRequest;
import com.corewise.modernization.web.dto.ProjectUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Projects, the unit of work we run for a client. Any signed-in team member can
 * work with projects. Moving a project to a new stage has its own endpoint so the
 * change is explicit.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projects;

    public ProjectController(ProjectService projects) {
        this.projects = projects;
    }

    @GetMapping
    public List<ProjectResponse> list(@RequestParam(required = false) Long clientId) {
        List<Project> found = (clientId == null) ? projects.list() : projects.listByClient(clientId);
        return found.stream().map(ProjectResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        return ProjectResponse.from(projects.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        Project created = projects.create(request.clientId(), request.name(), request.description());
        return ProjectResponse.from(created);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        Project updated = projects.update(id, request.name(), request.description());
        return ProjectResponse.from(updated);
    }

    @PutMapping("/{id}/status")
    public ProjectResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody ProjectStatusRequest request) {
        Project updated = projects.updateStatus(id, request.status());
        return ProjectResponse.from(updated);
    }
}
