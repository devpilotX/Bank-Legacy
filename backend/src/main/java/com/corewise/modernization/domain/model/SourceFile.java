package com.corewise.modernization.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * One old source file we took in for a project. The bytes live on disk; this row
 * holds the facts about the file. Maps to the source_files table.
 */
@Entity
@Table(name = "source_files")
public class SourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String language; // cobol, copybook, jcl, other

    @Column(name = "byte_size")
    private Long byteSize;

    @Column(name = "line_count")
    private Integer lineCount;

    @Column
    private String checksum;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(nullable = false)
    private String status; // received, analyzing, analyzed, rewritten, verified

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected SourceFile() {
    }

    public SourceFile(Long projectId, String filename, String language, Long byteSize,
                      Integer lineCount, String checksum, String contentType, String storageKey,
                      String status, Long uploadedBy) {
        this.projectId = projectId;
        this.filename = filename;
        this.language = language;
        this.byteSize = byteSize;
        this.lineCount = lineCount;
        this.checksum = checksum;
        this.contentType = contentType;
        this.storageKey = storageKey;
        this.status = status;
        this.uploadedBy = uploadedBy;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getFilename() {
        return filename;
    }

    public String getLanguage() {
        return language;
    }

    public Long getByteSize() {
        return byteSize;
    }

    public Integer getLineCount() {
        return lineCount;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
