package com.corewise.modernization.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** One part of the old system: a program, copybook, or data file. */
@Entity
@Table(name = "dependency_nodes")
public class DependencyNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String kind; // program, copybook, file, paragraph, unknown

    @Column(name = "source_file_id")
    private Long sourceFileId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected DependencyNode() {
    }

    public DependencyNode(Long projectId, String name, String kind, Long sourceFileId) {
        this.projectId = projectId;
        this.name = name;
        this.kind = kind;
        this.sourceFileId = sourceFileId;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public Long getSourceFileId() {
        return sourceFileId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
