package com.corewise.modernization.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * One run of a case: the engine ran the original COBOL and the new Java on the same
 * input and compared them. We keep the whole picture so an engineer can see exactly
 * what happened. Runs are kept, never changed. Maps to verification_runs.
 */
@Entity
@Table(name = "verification_runs")
public class VerificationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_unit_id", nullable = false)
    private Long workUnitId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(nullable = false)
    private boolean passed;

    @Column
    private String detail;

    /** The golden output from running the original COBOL. */
    @Column(name = "cobol_output")
    private String cobolOutput;

    /** What the new Java produced on the same input. */
    @Column(name = "java_output")
    private String javaOutput;

    /** A readable diff when the two differ. */
    @Column
    private String diff;

    /** passed, failed, a compile or run error on either side, a timeout, or engine error. */
    @Column
    private String outcome;

    /** none, formatting, or behavior. */
    @Column(name = "difference_kind")
    private String differenceKind;

    @Column(name = "normalized_trailing_space")
    private Boolean normalizedTrailingSpace;

    @Column(name = "numeric_tolerance")
    private Double numericTolerance;

    @Column(name = "run_by")
    private Long runBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected VerificationRun() {
    }

    public VerificationRun(Long workUnitId, Long caseId, boolean passed, String detail,
                           String cobolOutput, String javaOutput, String diff, String outcome,
                           String differenceKind, Boolean normalizedTrailingSpace,
                           Double numericTolerance, Long runBy) {
        this.workUnitId = workUnitId;
        this.caseId = caseId;
        this.passed = passed;
        this.detail = detail;
        this.cobolOutput = cobolOutput;
        this.javaOutput = javaOutput;
        this.diff = diff;
        this.outcome = outcome;
        this.differenceKind = differenceKind;
        this.normalizedTrailingSpace = normalizedTrailingSpace;
        this.numericTolerance = numericTolerance;
        this.runBy = runBy;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkUnitId() {
        return workUnitId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getDetail() {
        return detail;
    }

    public String getCobolOutput() {
        return cobolOutput;
    }

    public String getJavaOutput() {
        return javaOutput;
    }

    public String getDiff() {
        return diff;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getDifferenceKind() {
        return differenceKind;
    }

    public Boolean getNormalizedTrailingSpace() {
        return normalizedTrailingSpace;
    }

    public Double getNumericTolerance() {
        return numericTolerance;
    }

    public Long getRunBy() {
        return runBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
