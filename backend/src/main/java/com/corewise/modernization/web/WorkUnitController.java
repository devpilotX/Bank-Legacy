package com.corewise.modernization.web;

import com.corewise.modernization.service.WorkUnitService;
import com.corewise.modernization.web.dto.CreateWorkUnitRequest;
import com.corewise.modernization.web.dto.SaveJavaRequest;
import com.corewise.modernization.web.dto.WorkUnitResponse;
import com.corewise.modernization.web.dto.WorkUnitStatusRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The modernization workspace. An engineer creates a unit from a piece of old code,
 * asks the AI for a first-draft Java translation, edits it, moves it through the
 * stages, and approves the final. The AI draft and the human version are kept apart.
 */
@RestController
public class WorkUnitController {

    private final WorkUnitService workUnits;

    public WorkUnitController(WorkUnitService workUnits) {
        this.workUnits = workUnits;
    }

    @PostMapping("/api/projects/{projectId}/work-units")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkUnitResponse create(@PathVariable Long projectId,
                                   @Valid @RequestBody CreateWorkUnitRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        Long me = currentUserId(jwt);
        Long owner = request.ownerId() == null ? me : request.ownerId();
        return WorkUnitResponse.from(workUnits.create(projectId, request.title(),
            request.originalCode(), request.sourceFileId(), owner, request.notes(), me));
    }

    @GetMapping("/api/projects/{projectId}/work-units")
    public List<WorkUnitResponse> list(@PathVariable Long projectId) {
        return workUnits.list(projectId).stream().map(WorkUnitResponse::from).toList();
    }

    @GetMapping("/api/work-units/{id}")
    public WorkUnitResponse get(@PathVariable Long id) {
        return WorkUnitResponse.from(workUnits.getById(id));
    }

    @PostMapping("/api/work-units/{id}/ai-draft")
    public WorkUnitResponse aiDraft(@PathVariable Long id) {
        return WorkUnitResponse.from(workUnits.draftTranslation(id));
    }

    @PutMapping("/api/work-units/{id}/java")
    public WorkUnitResponse saveJava(@PathVariable Long id, @Valid @RequestBody SaveJavaRequest request) {
        return WorkUnitResponse.from(workUnits.saveHumanJava(id, request.java()));
    }

    @PutMapping("/api/work-units/{id}/status")
    public WorkUnitResponse moveStatus(@PathVariable Long id,
                                       @Valid @RequestBody WorkUnitStatusRequest request) {
        return WorkUnitResponse.from(workUnits.moveStatus(id, request.status()));
    }

    @PostMapping("/api/work-units/{id}/approve")
    public WorkUnitResponse approve(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return WorkUnitResponse.from(workUnits.approve(id, currentUserId(jwt)));
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
