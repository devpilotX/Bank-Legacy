package com.corewise.modernization.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * A test case is now just the inputs. The expected output is no longer typed by a
 * person; the engine gets it by running the original COBOL. Maps to verification_cases.
 */
@Entity
@Table(name = "verification_cases")
public class VerificationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_unit_id", nullable = false)
    private Long workUnitId;

    @Column(nullable = false)
    private String name;

    /** What we feed the program on standard input. May be empty. */
    @Column
    private String input;

    /** Optional JSON of file name to content, for programs that read input files. */
    @Column(name = "input_files")
    private String inputFiles;

    /** Optional and legacy. The engine now derives the expected output from the COBOL. */
    @Column(name = "expected_output")
    private String expectedOutput;

    @Column(nullable = false)
    private String origin; // human or ai

    @Column(nullable = false)
    private String status; // confirmed or suggested

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected VerificationCase() {
    }

    public VerificationCase(Long workUnitId, String name, String input, String inputFiles,
                            String origin, String status, Long createdBy) {
        this.workUnitId = workUnitId;
        this.name = name;
        this.input = input;
        this.inputFiles = inputFiles;
        this.origin = origin;
        this.status = status;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkUnitId() {
        return workUnitId;
    }

    public String getName() {
        return name;
    }

    public String getInput() {
        return input;
    }

    public String getInputFiles() {
        return inputFiles;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public String getOrigin() {
        return origin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
