package com.corewise.modernization.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** A link between two parts of the old system. */
@Entity
@Table(name = "dependency_edges")
public class DependencyEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "from_node_id", nullable = false)
    private Long fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private Long toNodeId;

    @Column(nullable = false)
    private String kind; // calls, includes, reads, writes, relates

    @Column(nullable = false)
    private String origin; // parsed or ai

    @Column(nullable = false)
    private String status; // confirmed, suggested, rejected

    @Column
    private String detail;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected DependencyEdge() {
    }

    public DependencyEdge(Long projectId, Long fromNodeId, Long toNodeId, String kind,
                          String origin, String status, String detail) {
        this.projectId = projectId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.kind = kind;
        this.origin = origin;
        this.status = status;
        this.detail = detail;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getFromNodeId() {
        return fromNodeId;
    }

    public Long getToNodeId() {
        return toNodeId;
    }

    public String getKind() {
        return kind;
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

    public String getDetail() {
        return detail;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
