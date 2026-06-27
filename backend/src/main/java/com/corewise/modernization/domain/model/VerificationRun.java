package com.corewise.modernization.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** One run of the new Java against one case, with the result. Maps to verification_runs. */
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

    @Column(name = "actual_output")
    private String actualOutput;

    @Column(nullable = false)
    private boolean passed;

    @Column
    private String detail;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected VerificationRun() {
    }

    public VerificationRun(Long workUnitId, Long caseId, String actualOutput, boolean passed, String detail) {
        this.workUnitId = workUnitId;
        this.caseId = caseId;
        this.actualOutput = actualOutput;
        this.passed = passed;
        this.detail = detail;
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

    public String getActualOutput() {
        return actualOutput;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getDetail() {
        return detail;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
