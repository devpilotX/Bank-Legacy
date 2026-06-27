package com.corewise.modernization.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * One piece of old code being rewritten into Java. Holds the original code, the AI's
 * first-draft Java, and the engineer's final Java, all at once. Maps to work_units.
 */
@Entity
@Table(name = "work_units")
public class WorkUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "source_file_id")
    private Long sourceFileId;

    @Column(nullable = false)
    private String title;

    @Column(name = "original_code", nullable = false)
    private String originalCode;

    @Column(name = "ai_draft_java")
    private String aiDraftJava;

    @Column(name = "human_java")
    private String humanJava;

    @Column(nullable = false)
    private String status; // todo, in_progress, in_review, done

    @Column(name = "owner_id")
    private Long ownerId;

    @Column
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected WorkUnit() {
    }

    public WorkUnit(Long projectId, Long sourceFileId, String title, String originalCode,
                    String status, Long ownerId, String notes, Long createdBy) {
        this.projectId = projectId;
        this.sourceFileId = sourceFileId;
        this.title = title;
        this.originalCode = originalCode;
        this.status = status;
        this.ownerId = ownerId;
        this.notes = notes;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getSourceFileId() {
        return sourceFileId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public String getAiDraftJava() {
        return aiDraftJava;
    }

    public void setAiDraftJava(String aiDraftJava) {
        this.aiDraftJava = aiDraftJava;
    }

    public String getHumanJava() {
        return humanJava;
    }

    public void setHumanJava(String humanJava) {
        this.humanJava = humanJava;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
