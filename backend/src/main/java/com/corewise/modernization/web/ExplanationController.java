package com.corewise.modernization.web;

import com.corewise.modernization.service.AiExplanationService;
import com.corewise.modernization.web.dto.EditExplanationRequest;
import com.corewise.modernization.web.dto.ExplainRequest;
import com.corewise.modernization.web.dto.ExplanationResponse;
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
 * Reading old code with the AI. An engineer asks for an explanation of a file or a
 * slice of it, then reviews, edits, and approves the result. The AI's output is
 * always a draft; editing it sends it back to draft so it gets approved again.
 */
@RestController
public class ExplanationController {

    private final AiExplanationService explanations;

    public ExplanationController(AiExplanationService explanations) {
        this.explanations = explanations;
    }

    @PostMapping("/api/files/{fileId}/explanations")
    @ResponseStatus(HttpStatus.CREATED)
    public ExplanationResponse explain(@PathVariable Long fileId,
                                       @RequestBody(required = false) ExplainRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        ExplainRequest safe = request == null ? new ExplainRequest(null, null) : request;
        return ExplanationResponse.from(
            explanations.explain(fileId, safe.startLine(), safe.endLine(), currentUserId(jwt)));
    }

    @GetMapping("/api/files/{fileId}/explanations")
    public List<ExplanationResponse> list(@PathVariable Long fileId) {
        return explanations.listForFile(fileId).stream().map(ExplanationResponse::from).toList();
    }

    @PutMapping("/api/explanations/{id}")
    public ExplanationResponse edit(@PathVariable Long id, @Valid @RequestBody EditExplanationRequest request) {
        return ExplanationResponse.from(explanations.edit(id, request.content()));
    }

    @PostMapping("/api/explanations/{id}/approve")
    public ExplanationResponse approve(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ExplanationResponse.from(explanations.approve(id, currentUserId(jwt)));
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
