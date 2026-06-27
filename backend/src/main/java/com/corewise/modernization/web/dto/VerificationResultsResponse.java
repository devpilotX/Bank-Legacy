package com.corewise.modernization.web.dto;

import com.corewise.modernization.service.VerificationService;
import java.util.List;

/** The whole verification picture for a unit: each case with its latest run, plus totals. */
public record VerificationResultsResponse(
    int total,
    int passed,
    int failed,
    int notRun,
    List<Item> cases
) {
    public record Item(VerificationCaseResponse verificationCase, VerificationRunResponse latestRun) {
    }

    public static VerificationResultsResponse from(VerificationService.Results results) {
        List<Item> items = results.cases().stream()
            .map(caseResult -> new Item(
                VerificationCaseResponse.from(caseResult.verificationCase()),
                caseResult.latestRun() == null ? null : VerificationRunResponse.from(caseResult.latestRun())))
            .toList();
        return new VerificationResultsResponse(results.total(), results.passed(),
            results.failed(), results.notRun(), items);
    }
}
