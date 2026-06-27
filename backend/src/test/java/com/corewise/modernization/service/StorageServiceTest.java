package com.corewise.modernization.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.corewise.modernization.config.StorageProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService storage;

    @BeforeEach
    void setUp() {
        storage = new StorageService(new StorageProperties(tempDir.toString()));
    }

    private InputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void storesBytesAndReportsSizeChecksumAndLines() throws Exception {
        String content = "IDENTIFICATION DIVISION.\nPROGRAM-ID. HELLO.\n";
        StorageService.Stored stored = storage.store(1L, "HELLO.cbl", stream(content));

        assertThat(stored.size()).isEqualTo(content.getBytes(StandardCharsets.UTF_8).length);
        assertThat(stored.lineCount()).isEqualTo(2);
        assertThat(stored.storageKey()).startsWith("project-1/");
        assertThat(stored.checksum()).hasSize(64); // SHA-256 as hex

        try (InputStream in = storage.open(stored.storageKey())) {
            String readBack = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(readBack).isEqualTo(content);
        }
    }

    @Test
    void countsTheLastLineEvenWithoutATrailingNewline() {
        StorageService.Stored stored = storage.store(2L, "two.txt", stream("line one\nline two"));
        assertThat(stored.lineCount()).isEqualTo(2);
    }

    @Test
    void anEmptyFileHasZeroLines() {
        StorageService.Stored stored = storage.store(3L, "empty.txt", stream(""));
        assertThat(stored.size()).isZero();
        assertThat(stored.lineCount()).isZero();
    }
}
