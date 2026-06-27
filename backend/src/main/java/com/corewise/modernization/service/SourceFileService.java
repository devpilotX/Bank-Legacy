package com.corewise.modernization.service;

import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.SourceFile;
import com.corewise.modernization.repository.SourceFileRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Takes in old source files for a project. A single file or a whole zip of files
 * both work. We never load a whole upload into memory: each file is streamed to
 * disk, and for a zip we stream one entry at a time.
 */
@Service
public class SourceFileService {

    public static final String STATUS_RECEIVED = "received";

    // The kinds of files we accept on their own. A zip may carry these too. We reject
    // anything else, so the tool never takes in a random binary.
    private static final Set<String> ALLOWED_EXTENSIONS =
        Set.of("cbl", "cob", "cobol", "cpy", "cpybk", "jcl", "pli", "pl1", "asm", "txt", "dat");

    private final SourceFileRepository files;
    private final StorageService storage;
    private final ProjectService projects;

    public SourceFileService(SourceFileRepository files, StorageService storage, ProjectService projects) {
        this.files = files;
        this.storage = storage;
        this.projects = projects;
    }

    @Transactional
    public SourceFile uploadFile(Long projectId, Long uploadedBy, MultipartFile file) {
        projects.getById(projectId);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was uploaded, or it was empty.");
        }
        requireAllowedSourceFile(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            return saveStream(projectId, uploadedBy, file.getOriginalFilename(), file.getContentType(), in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the uploaded file.", e);
        }
    }

    @Transactional
    public List<SourceFile> uploadZip(Long projectId, Long uploadedBy, MultipartFile zip) {
        projects.getById(projectId);
        if (zip == null || zip.isEmpty()) {
            throw new BadRequestException("No zip was uploaded, or it was empty.");
        }
        String zipName = zip.getOriginalFilename();
        if (zipName == null || !zipName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new BadRequestException("Please upload a .zip file.");
        }
        List<SourceFile> saved = new ArrayList<>();
        try (ZipInputStream zipIn = new ZipInputStream(zip.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // The storage service reads this entry to its end without closing the
                    // zip stream, so we move through the zip one entry at a time.
                    saved.add(saveStream(projectId, uploadedBy, entry.getName(), null, zipIn));
                }
                zipIn.closeEntry();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the zip file.", e);
        }
        if (saved.isEmpty()) {
            throw new BadRequestException("That zip did not contain any files.");
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SourceFile> list(Long projectId) {
        projects.getById(projectId);
        return files.findByProjectIdOrderByFilenameAsc(projectId);
    }

    @Transactional(readOnly = true)
    public SourceFile getById(Long id) {
        return files.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("There is no file with id " + id + "."));
    }

    /** Opens the stored bytes of a file so a controller can stream them out. */
    public InputStream openContent(SourceFile file) {
        if (file.getStorageKey() == null) {
            throw new ResourceNotFoundException("This file has no stored content.");
        }
        return storage.open(file.getStorageKey());
    }

    private SourceFile saveStream(Long projectId, Long uploadedBy, String rawName,
                                  String contentType, InputStream in) {
        String filename = (rawName == null || rawName.isBlank()) ? "file" : rawName;
        StorageService.Stored stored = storage.store(projectId, filename, in);
        SourceFile sourceFile = new SourceFile(projectId, filename, detectLanguage(filename),
            stored.size(), stored.lineCount(), stored.checksum(), contentType, stored.storageKey(),
            STATUS_RECEIVED, uploadedBy);
        return files.save(sourceFile);
    }

    private void requireAllowedSourceFile(String filename) {
        if (!ALLOWED_EXTENSIONS.contains(extensionOf(filename))) {
            throw new BadRequestException(
                "We only take in source files like COBOL, copybooks, and JCL. That file type is not allowed.");
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    /** A rough guess at the language from the file extension, good enough to group files. */
    private String detectLanguage(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".cbl") || lower.endsWith(".cob") || lower.endsWith(".cobol")) {
            return "cobol";
        }
        if (lower.endsWith(".cpy")) {
            return "copybook";
        }
        if (lower.endsWith(".jcl")) {
            return "jcl";
        }
        return "other";
    }
}
