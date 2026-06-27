package com.corewise.modernization.service;

import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.config.StorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Saves uploaded files to disk and reads them back.
 *
 * We read the input in small chunks and write it straight to disk, so a huge COBOL
 * file or a big zip never has to sit fully in memory. While the bytes flow past, we
 * also add up the size, the SHA-256 checksum, and the line count in the same single
 * pass, which is cheap and saves reading the file again.
 */
@Service
public class StorageService {

    private static final int BUFFER_SIZE = 8192;

    private final Path root;

    public StorageService(StorageProperties properties) {
        this.root = Paths.get(properties.root()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create the file storage folder at " + root, e);
        }
    }

    /**
     * Streams the input to disk under the given project and returns the facts we
     * gathered about it. The original name is only used to build a readable key; the
     * stored name is made unique so two files with the same name never collide.
     */
    public Stored store(long projectId, String originalFilename, InputStream input) {
        String safeName = sanitize(originalFilename);
        String storageKey = "project-" + projectId + "/" + UUID.randomUUID() + "-" + safeName;
        Path target = root.resolve(storageKey).normalize();
        // Guard against a crafted name trying to escape the storage folder.
        if (!target.startsWith(root)) {
            throw new BadRequestException("That file name is not allowed.");
        }

        try {
            Files.createDirectories(target.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long size = 0;
            long newlines = 0;
            int lastByte = -1;
            byte[] buffer = new byte[BUFFER_SIZE];

            try (OutputStream out = Files.newOutputStream(target)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    digest.update(buffer, 0, read);
                    size += read;
                    for (int i = 0; i < read; i++) {
                        if (buffer[i] == (byte) '\n') {
                            newlines++;
                        }
                    }
                    if (read > 0) {
                        lastByte = buffer[read - 1] & 0xff;
                    }
                }
            }

            long lines = lineCount(size, newlines, lastByte);
            String checksum = HexFormat.of().formatHex(digest.digest());
            return new Stored(storageKey, size, checksum, (int) Math.min(lines, Integer.MAX_VALUE));
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the file to disk.", e);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always present in a standard JVM, so this should never happen.
            throw new IllegalStateException("SHA-256 is not available on this JVM.", e);
        }
    }

    /** Opens the stored bytes for reading, for example to stream a download. */
    public InputStream open(String storageKey) {
        Path path = root.resolve(storageKey).normalize();
        if (!path.startsWith(root)) {
            throw new BadRequestException("That file path is not allowed.");
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the stored file.", e);
        }
    }

    private long lineCount(long size, long newlines, int lastByte) {
        if (size == 0) {
            return 0;
        }
        // If the file does not end with a newline, the last line still counts.
        return lastByte == '\n' ? newlines : newlines + 1;
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        // Keep only the base name and strip anything that is not safe in a path.
        String base = Paths.get(name).getFileName().toString();
        String cleaned = base.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.isBlank() ? "file" : cleaned;
    }

    /** The facts we learned while saving a file. */
    public record Stored(String storageKey, long size, String checksum, int lineCount) {
    }
}
