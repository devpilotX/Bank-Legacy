package com.corewise.modernization.web;

import com.corewise.modernization.service.VerificationService;
import com.corewise.modernization.web.dto.AddVerificationCaseRequest;
import com.corewise.modernization.web.dto.RunVerificationRequest;
import com.corewise.modernization.web.dto.VerificationCaseResponse;
import com.corewise.modernization.web.dto.VerificationResultsResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verification: proving the new Java behaves like the old code. Engineers add input
 * cases by hand or let the AI draft some to confirm, then press run. The engine runs the
 * old COBOL and the new Java on each case and reports a real pass or fail with a diff.
 */
@RestController
public class VerificationController {

    private final VerificationService verification;

    public VerificationController(VerificationService verification) {
        this.verification = verification;
    }

    @PostMapping("/api/work-units/{workUnitId}/verification-cases")
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationCaseResponse addCase(@PathVariable Long workUnitId,
                                            @Valid @RequestBody AddVerificationCaseRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return VerificationCaseResponse.from(verification.addCase(workUnitId, request.name(),
            request.input(), request.inputFiles(), currentUserId(jwt)));
    }

    @PostMapping("/api/work-units/{workUnitId}/verification-cases/ai-draft")
    public List<VerificationCaseResponse> draftCases(@PathVariable Long workUnitId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return verification.draftCases(workUnitId, currentUserId(jwt)).stream()
            .map(VerificationCaseResponse::from)
            .toList();
    }

    @GetMapping("/api/work-units/{workUnitId}/verification-cases")
    public List<VerificationCaseResponse> listCases(@PathVariable Long workUnitId) {
        return verification.listCases(workUnitId).stream().map(VerificationCaseResponse::from).toList();
    }

    @PostMapping("/api/verification-cases/{caseId}/confirm")
    public VerificationCaseResponse confirm(@PathVariable Long caseId) {
        return VerificationCaseResponse.from(verification.confirmCase(caseId));
    }

    @PostMapping("/api/work-units/{workUnitId}/verify")
    public VerificationResultsResponse verify(@PathVariable Long workUnitId,
                                              @RequestBody(required = false) RunVerificationRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        RunVerificationRequest safe =
            request == null ? new RunVerificationRequest(null, null, null) : request;
        return VerificationResultsResponse.from(verification.runForUnit(workUnitId,
            safe.caseIds(), safe.trimTrailingSpace(), safe.numericTolerance(), currentUserId(jwt)));
    }

    @GetMapping("/api/work-units/{workUnitId}/verification")
    public VerificationResultsResponse results(@PathVariable Long workUnitId) {
        return VerificationResultsResponse.from(verification.getResults(workUnitId));
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
