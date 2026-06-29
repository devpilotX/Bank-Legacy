package com.corewise.modernization.service;

import com.corewise.modernization.ai.AiClient;
import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.VerificationCase;
import com.corewise.modernization.domain.model.VerificationRun;
import com.corewise.modernization.domain.model.WorkUnit;
import com.corewise.modernization.repository.VerificationCaseRepository;
import com.corewise.modernization.repository.VerificationRunRepository;
import com.corewise.modernization.verify.EngineResult;
import com.corewise.modernization.verify.VerificationEngine;
import com.corewise.modernization.verify.VerifyProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verification proves the new Java behaves like the old code. A case is a set of inputs.
 * The engine runs the original COBOL on those inputs to get the true expected output,
 * runs the new Java the same way, and compares them. A person no longer types the
 * expected output by hand; the old COBOL gives us the golden answer.
 *
 * <p>The AI can draft input cases from the old code, but those start as suggestions a
 * human confirms before we run them.
 */
@Service
public class VerificationService {

    public static final String ORIGIN_HUMAN = "human";
    public static final String ORIGIN_AI = "ai";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_SUGGESTED = "suggested";

    private static final String DRAFT_SYSTEM_PROMPT = """
        You write test inputs for old mainframe code that is being rewritten. Propose up to
        five small cases that pin down its behavior. Put one case per line in the form
        NAME | INPUT, where INPUT is what the program reads on standard input. If a case
        needs no input, put a single dash for the input. Do not include the expected output;
        we get that by running the original program. Reply with nothing else.""";

    private final VerificationCaseRepository cases;
    private final VerificationRunRepository runs;
    private final WorkUnitService workUnits;
    private final AiClient aiClient;
    private final VerificationEngine engine;
    private final VerifyProperties verifyProps;
    private final ObjectMapper objectMapper;

    public VerificationService(VerificationCaseRepository cases, VerificationRunRepository runs,
                               WorkUnitService workUnits, AiClient aiClient,
                               VerificationEngine engine, VerifyProperties verifyProps,
                               ObjectMapper objectMapper) {
        this.cases = cases;
        this.runs = runs;
        this.workUnits = workUnits;
        this.aiClient = aiClient;
        this.engine = engine;
        this.verifyProps = verifyProps;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public VerificationCase addCase(Long workUnitId, String name, String input,
                                    String inputFiles, Long createdBy) {
        workUnits.getById(workUnitId);
        return cases.save(new VerificationCase(workUnitId, name.trim(), emptyToNull(input),
            emptyToNull(inputFiles), ORIGIN_HUMAN, STATUS_CONFIRMED, createdBy));
    }

    @Transactional
    public List<VerificationCase> draftCases(Long workUnitId, Long createdBy) {
        WorkUnit unit = workUnits.getById(workUnitId);
        String answer = aiClient.complete(DRAFT_SYSTEM_PROMPT, unit.getOriginalCode());
        List<VerificationCase> saved = new ArrayList<>();
        for (String line : answer.split("\\r?\\n")) {
            String[] parts = line.split("\\|", 2);
            if (parts.length < 1) {
                continue;
            }
            String name = parts[0].trim();
            String input = parts.length > 1 ? parts[1].trim() : "";
            if (name.isEmpty()) {
                continue;
            }
            saved.add(cases.save(new VerificationCase(workUnitId, name,
                "-".equals(input) ? null : emptyToNull(input), null,
                ORIGIN_AI, STATUS_SUGGESTED, createdBy)));
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

    /**
     * Run the real engine for a unit. We run every confirmed case (or just the ones asked
     * for), compiling and running both the COBOL and the Java and comparing them. The
     * comparison settings are explicit and stored with each run.
     */
    @Transactional
    public Results runForUnit(Long workUnitId, List<Long> caseIds, Boolean trimTrailingSpace,
                              Double numericTolerance, Long runBy) {
        WorkUnit unit = workUnits.getById(workUnitId);
        boolean trim = trimTrailingSpace != null ? trimTrailingSpace : verifyProps.trimTrailingSpace();
        double tolerance = numericTolerance != null ? numericTolerance : verifyProps.numericTolerance();

        List<VerificationCase> confirmed = cases.findByWorkUnitIdOrderByCreatedAtAsc(workUnitId).stream()
            .filter(c -> STATUS_CONFIRMED.equals(c.getStatus()))
            .filter(c -> caseIds == null || caseIds.isEmpty() || caseIds.contains(c.getId()))
            .toList();
        if (confirmed.isEmpty()) {
            throw new BadRequestException("Confirm at least one case before running the checks.");
        }

        for (VerificationCase verificationCase : confirmed) {
            EngineResult result = engine.run(unit.getOriginalCode(), unit.getHumanJava(),
                verificationCase.getInput(), parseInputFiles(verificationCase.getInputFiles()),
                trim, tolerance);
            runs.save(new VerificationRun(
                workUnitId,
                verificationCase.getId(),
                result.passed(),
                result.detail(),
                result.cobolOutput(),
                result.javaOutput(),
                result.diff(),
                result.outcome().dbValue(),
                result.differenceKind() == null
                    ? null : result.differenceKind().name().toLowerCase(Locale.ROOT),
                result.trimmedTrailingSpace(),
                result.numericTolerance(),
                runBy));
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

    private Map<String, String> parseInputFiles(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, String> parsed = objectMapper.readValue(json, new TypeReference<>() {});
            return parsed == null ? Map.of() : parsed;
        } catch (Exception e) {
            // A malformed file map should not crash a run. Treat it as no files; the COBOL
            // will then report it cannot find the file, which is a clear result on its own.
            return Map.of();
        }
    }

    private VerificationCase getCase(Long caseId) {
        return cases.findById(caseId)
            .orElseThrow(() -> new ResourceNotFoundException("There is no test case with id " + caseId + "."));
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /** A case paired with its most recent run (which may be null if never run). */
    public record CaseResult(VerificationCase verificationCase, VerificationRun latestRun) {
    }

    /** The full verification picture for a work unit. */
    public record Results(List<CaseResult> cases, int total, int passed, int failed, int notRun) {
    }
}
