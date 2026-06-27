package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.SourceFile;
import java.time.OffsetDateTime;

/** What we tell the frontend about a stored source file. */
public record SourceFileResponse(
    Long id,
    Long projectId,
    String filename,
    String language,
    Long byteSize,
    Integer lineCount,
    String checksum,
    String status,
    Long uploadedBy,
    OffsetDateTime createdAt
) {
    public static SourceFileResponse from(SourceFile file) {
        return new SourceFileResponse(file.getId(), file.getProjectId(), file.getFilename(),
            file.getLanguage(), file.getByteSize(), file.getLineCount(), file.getChecksum(),
            file.getStatus(), file.getUploadedBy(), file.getCreatedAt());
    }
}
