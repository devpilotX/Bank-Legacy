package com.corewise.modernization.service;

import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.Project;
import com.corewise.modernization.repository.ProjectRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creating, listing, and moving projects through their stages. */
@Service
public class ProjectService {

    public static final String STAGE_INTAKE = "intake";

    /** The stages a project moves through, in order. */
    public static final Set<String> STAGES =
        Set.of("intake", "mapping", "modernizing", "verifying", "done");

    private final ProjectRepository projects;
    private final ClientService clients;

    public ProjectService(ProjectRepository projects, ClientService clients) {
        this.projects = projects;
        this.clients = clients;
    }

    @Transactional
    public Project create(Long clientId, String name, String description) {
        // A project must belong to a real client, so we check that first.
        clients.getById(clientId);
        return projects.save(new Project(clientId, name.trim(), trimToNull(description), STAGE_INTAKE));
    }

    @Transactional(readOnly = true)
    public List<Project> list() {
        return projects.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public List<Project> listByClient(Long clientId) {
        clients.getById(clientId);
        return projects.findByClientIdOrderByNameAsc(clientId);
    }

    @Transactional(readOnly = true)
    public Project getById(Long id) {
        return projects.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("There is no project with id " + id + "."));
    }

    @Transactional
    public Project update(Long id, String name, String description) {
        Project project = getById(id);
        if (name != null && !name.isBlank()) {
            project.setName(name.trim());
        }
        project.setDescription(trimToNull(description));
        return projects.save(project);
    }

    @Transactional
    public Project updateStatus(Long id, String status) {
        if (status == null || !STAGES.contains(status)) {
            throw new BadRequestException(
                "Project stage must be one of: intake, mapping, modernizing, verifying, done.");
        }
        Project project = getById(id);
        project.setStatus(status);
        return projects.save(project);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
