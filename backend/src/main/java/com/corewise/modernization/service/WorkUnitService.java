package com.corewise.modernization.service;

import com.corewise.modernization.ai.AiClient;
import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.WorkUnit;
import com.corewise.modernization.repository.WorkUnitRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The modernization workspace: rewriting old code into Java one small piece at a
 * time. The AI writes a first draft; the engineer edits and approves the final. We
 * keep both, so we always know what the AI suggested versus what a person shipped.
 */
@Service
public class WorkUnitService {

    public static final String STATUS_TODO = "todo";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final Set<String> STATUSES = Set.of("todo", "in_progress", "in_review", "done");
    private static final String STATUS_DONE = "done";

    private static final String TRANSLATE_SYSTEM = """
        You translate old COBOL into clean, modern Java for engineers who will review your
        work. Produce a faithful first draft that keeps the same behavior. Prefer clear,
        plain Java over clever tricks. Where something cannot be translated faithfully, do
        not guess: leave a // TODO comment that explains what a human needs to decide.
        Reply with Java code only.""";

    private final WorkUnitRepository workUnits;
    private final ProjectService projects;
    private final AiClient aiClient;

    public WorkUnitService(WorkUnitRepository workUnits, ProjectService projects, AiClient aiClient) {
        this.workUnits = workUnits;
        this.projects = projects;
        this.aiClient = aiClient;
    }

    @Transactional
    public WorkUnit create(Long projectId, String title, String originalCode, Long sourceFileId,
                           Long ownerId, String notes, Long createdBy) {
        projects.getById(projectId);
        WorkUnit unit = new WorkUnit(projectId, sourceFileId, title.trim(), originalCode,
            STATUS_TODO, ownerId, notes, createdBy);
        return workUnits.save(unit);
    }

    @Transactional(readOnly = true)
    public List<WorkUnit> list(Long projectId) {
        projects.getById(projectId);
        return workUnits.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    @Transactional(readOnly = true)
    public WorkUnit getById(Long id) {
        return workUnits.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("There is no work unit with id " + id + "."));
    }

    @Transactional
    public WorkUnit draftTranslation(Long id) {
        WorkUnit unit = getById(id);
        String java = aiClient.complete(TRANSLATE_SYSTEM, unit.getOriginalCode());
        unit.setAiDraftJava(java);
        // A draft exists now, so the unit is underway if it had not started.
        if (STATUS_TODO.equals(unit.getStatus())) {
            unit.setStatus(STATUS_IN_PROGRESS);
        }
        return workUnits.save(unit);
    }

    @Transactional
    public WorkUnit saveHumanJava(Long id, String java) {
        WorkUnit unit = getById(id);
        unit.setHumanJava(java);
        return workUnits.save(unit);
    }

    @Transactional
    public WorkUnit moveStatus(Long id, String status) {
        if (status == null || !STATUSES.contains(status)) {
            throw new BadRequestException("Status must be one of: todo, in_progress, in_review, done.");
        }
        WorkUnit unit = getById(id);
        unit.setStatus(status);
        return workUnits.save(unit);
    }

    @Transactional
    public WorkUnit approve(Long id, Long approvedBy) {
        WorkUnit unit = getById(id);
        // We never approve an empty rewrite. A person must have written the Java first.
        if (unit.getHumanJava() == null || unit.getHumanJava().isBlank()) {
            throw new BadRequestException("Add the human-reviewed Java before approving this unit.");
        }
        unit.setStatus(STATUS_DONE);
        unit.setApprovedBy(approvedBy);
        unit.setApprovedAt(OffsetDateTime.now());
        return workUnits.save(unit);
    }
}
