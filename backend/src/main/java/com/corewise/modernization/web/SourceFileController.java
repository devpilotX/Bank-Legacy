package com.corewise.modernization.web;

import com.corewise.modernization.domain.model.SourceFile;
import com.corewise.modernization.service.SourceFileService;
import com.corewise.modernization.web.dto.SourceFileResponse;
import java.io.InputStream;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Taking in old code and reading it back. Any signed-in team member can upload to a
 * project, list its files, and download one. Downloads are streamed straight from
 * disk, so even a very large file does not get buffered in memory.
 */
@RestController
public class SourceFileController {

    private final SourceFileService sourceFiles;

    public SourceFileController(SourceFileService sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    @PostMapping(value = "/api/projects/{projectId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SourceFileResponse upload(@PathVariable Long projectId,
                                     @RequestParam("file") MultipartFile file,
                                     @AuthenticationPrincipal Jwt jwt) {
        SourceFile saved = sourceFiles.uploadFile(projectId, currentUserId(jwt), file);
        return SourceFileResponse.from(saved);
    }

    @PostMapping(value = "/api/projects/{projectId}/files/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<SourceFileResponse> uploadZip(@PathVariable Long projectId,
                                              @RequestParam("file") MultipartFile file,
                                              @AuthenticationPrincipal Jwt jwt) {
        return sourceFiles.uploadZip(projectId, currentUserId(jwt), file).stream()
            .map(SourceFileResponse::from)
            .toList();
    }

    @GetMapping("/api/projects/{projectId}/files")
    public List<SourceFileResponse> list(@PathVariable Long projectId) {
        return sourceFiles.list(projectId).stream().map(SourceFileResponse::from).toList();
    }

    @GetMapping("/api/files/{id}")
    public SourceFileResponse get(@PathVariable Long id) {
        return SourceFileResponse.from(sourceFiles.getById(id));
    }

    @GetMapping("/api/files/{id}/content")
    public ResponseEntity<StreamingResponseBody> content(@PathVariable Long id) {
        SourceFile file = sourceFiles.getById(id);
        StreamingResponseBody body = out -> {
            try (InputStream in = sourceFiles.openContent(file)) {
                in.transferTo(out);
            }
        };
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName(file.getFilename()) + "\"")
            .body(body);
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private String safeName(String filename) {
        // Keep the header simple and safe: just the base name, no quotes or newlines.
        String base = filename == null ? "file" : filename.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        return base.replaceAll("[\"\\r\\n]", "_");
    }
}
