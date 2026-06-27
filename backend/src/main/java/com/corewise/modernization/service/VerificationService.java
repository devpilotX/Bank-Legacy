package com.corewise.modernization.service;

import com.corewise.modernization.ai.AiClient;
import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.VerificationCase;
import com.corewise.modernization.domain.model.VerificationRun;
import com.corewise.modernization.domain.model.WorkUnit;
import com.corewise.modernization.repository.VerificationCaseRepository;
import com.corewise.modernization.repository.VerificationRunRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks that the new Java behaves like the old code. A case is an input and the
 * output the old system gave. A run records what the new Java gave and whether it
 * matched. The AI can draft cases from the old code, but those are suggestions until
 * a person confirms them, so we never test against an unchecked expectation.
 */
@Service
public class VerificationService {

    public static final String ORIGIN_HUMAN = "human";
    public static final String ORIGIN_AI = "ai";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_SUGGESTED = "suggested";

    private static final String DRAFT_SYSTEM_PROMPT = """
        You write test cases for old mainframe code that is being rewritten. Propose up to
        five small cases that pin down its behavior. Put one case per line in the form
        NAME | INPUT | EXPECTED_OUTPUT. Keep each field to a single line. If a case needs no
        input, put a single dash for the input. Reply with nothing else.""";

    private final VerificationCaseRepository cases;
    private final VerificationRunRepository runs;
    private final WorkUnitService workUnits;
    private final AiClient aiClient;

    public VerificationService(VerificationCaseRepository cases, VerificationRunRepository runs,
                               WorkUnitService workUnits, AiClient aiClient) {
        this.cases = cases;
        this.runs = runs;
        this.workUnits = workUnits;
        this.aiClient = aiClient;
    }

    @Transactional
    public VerificationCase addCase(Long workUnitId, String name, String input,
                                    String expectedOutput, Long createdBy) {
        workUnits.getById(workUnitId);
        return cases.save(new VerificationCase(workUnitId, name.trim(), input, expectedOutput,
            ORIGIN_HUMAN, STATUS_CONFIRMED, createdBy));
    }

    @Transactional
    public List<VerificationCase> draftCases(Long workUnitId, Long createdBy) {
        WorkUnit unit = workUnits.getById(workUnitId);
        String answer = aiClient.complete(DRAFT_SYSTEM_PROMPT, unit.getOriginalCode());
        List<VerificationCase> saved = new ArrayList<>();
        for (String line : answer.split("\\r?\\n")) {
            String[] parts = line.split("\\|", 3);
            if (parts.length < 3) {
                continue;
            }
            String name = parts[0].trim();
            String input = parts[1].trim();
            String expected = parts[2].trim();
            if (name.isEmpty() || expected.isEmpty()) {
                continue;
            }
            saved.add(cases.save(new VerificationCase(workUnitId, name,
                "-".equals(input) ? null : input, expected, ORIGIN_AI, STATUS_SUGGESTED, createdBy)));
        }
        if (saved.isEmpty()) {
            throw new BadRequestException(
                "The AI did not return any usable test cases. Try again, or add cases by hand.");
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<VerificationCase> listCases(Long workUnitId) {
        workUnits.getById(workUnitId);
        return cases.findByWorkUnitIdOrderByCreatedAtAsc(workUnitId);
    }

    @Transactional
    public VerificationCase confirmCase(Long caseId) {
        VerificationCase verificationCase = getCase(caseId);
        verificationCase.setStatus(STATUS_CONFIRMED);
        return cases.save(verificationCase);
    }

    @Transactional
    public Results runForUnit(Long workUnitId, List<CaseActual> actuals) {
        workUnits.getById(workUnitId);
        for (CaseActual actual : actuals) {
            VerificationCase verificationCase = getCase(actual.caseId());
            if (!verificationCase.getWorkUnitId().equals(workUnitId)) {
                throw new BadRequestException("Case " + actual.caseId()
                    + " does not belong to this work unit.");
            }
            if (!STATUS_CONFIRMED.equals(verificationCase.getStatus())) {
                throw new BadRequestException("Confirm case " + actual.caseId() + " before running it.");
            }
            VerificationComparator.Result result =
                VerificationComparator.evaluate(verificationCase.getExpectedOutput(), actual.actualOutput());
            runs.save(new VerificationRun(workUnitId, verificationCase.getId(),
                actual.actualOutput(), result.passed(), result.detail()));
        }
        return getResults(workUnitId);
    }

    @Transactional(readOnly = true)
    public Results getResults(Long workUnitId) {
        workUnits.getById(workUnitId);
        List<VerificationCase> caseList = cases.findByWorkUnitIdOrderByCreatedAtAsc(workUnitId);
        List<CaseResult> results = new ArrayList<>();
        int passed = 0;
        int failed = 0;
        int notRun = 0;
        for (VerificationCase verificationCase : caseList) {
            VerificationRun latest = runs.findFirstByCaseIdOrderByCreatedAtDesc(verificationCase.getId())
                .orElse(null);
            if (latest == null) {
                notRun++;
            } else if (latest.isPassed()) {
                passed++;
            } else {
                failed++;
            }
            results.add(new CaseResult(verificationCase, latest));
        }
        return new Results(results, caseList.size(), passed, failed, notRun);
    }

    private VerificationCase getCase(Long caseId) {
        return cases.findById(caseId)
            .orElseThrow(() -> new ResourceNotFoundException("There is no test case with id " + caseId + "."));
    }

    /** One case's actual output, submitted when running verification. */
    public record CaseActual(Long caseId, String actualOutput) {
    }

    /** A case paired with its most recent run (which may be null if never run). */
    public record CaseResult(VerificationCase verificationCase, VerificationRun latestRun) {
    }

    /** The full verification picture for a work unit. */
    public record Results(List<CaseResult> cases, int total, int passed, int failed, int notRun) {
    }
}
