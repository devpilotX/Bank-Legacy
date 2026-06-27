package com.corewise.modernization.service;

import com.corewise.modernization.ai.AiClient;
import com.corewise.modernization.ai.AiProperties;
import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.Explanation;
import com.corewise.modernization.domain.model.SourceFile;
import com.corewise.modernization.repository.ExplanationRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asks the AI to explain a file or a slice of it, and keeps the answer as a draft a
 * person must approve. The AI is the fast first read; an engineer always checks it.
 */
@Service
public class AiExplanationService {

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_APPROVED = "approved";

    // We cap how much code we send so one enormous file does not blow up the cost or
    // the request size. An engineer can explain a smaller slice with start/end lines.
    private static final int MAX_CODE_CHARS = 200_000;

    private static final String SYSTEM_PROMPT = """
        You read old COBOL and related mainframe code for experienced engineers who are
        modernizing it. Explain, in plain English, what this code does: its purpose, its
        inputs and outputs, and any side effects or gotchas. Be precise and concrete. If
        something cannot be known from the code alone, say so plainly instead of guessing.""";

    private final ExplanationRepository explanations;
    private final SourceFileService sourceFiles;
    private final AiClient aiClient;
    private final AiProperties aiProperties;

    public AiExplanationService(ExplanationRepository explanations, SourceFileService sourceFiles,
                                AiClient aiClient, AiProperties aiProperties) {
        this.explanations = explanations;
        this.sourceFiles = sourceFiles;
        this.aiClient = aiClient;
        this.aiProperties = aiProperties;
    }

    @Transactional
    public Explanation explain(Long fileId, Integer startLine, Integer endLine, Long requestedBy) {
        SourceFile file = sourceFiles.getById(fileId);
        String code = readCode(file, startLine, endLine);
        if (code.isBlank()) {
            throw new BadRequestException("There is no code in that range to explain.");
        }

        String range = (startLine == null && endLine == null)
            ? "the whole file"
            : "lines " + (startLine == null ? 1 : startLine) + " to "
                + (endLine == null ? "the end" : endLine);
        String userPrompt = "File: " + file.getFilename() + " (" + range + ")\n\n" + code;

        String answer = aiClient.complete(SYSTEM_PROMPT, userPrompt);

        Explanation explanation = new Explanation(fileId, startLine, endLine, answer,
            activeModel(), STATUS_DRAFT, requestedBy);
        return explanations.save(explanation);
    }

    @Transactional(readOnly = true)
    public List<Explanation> listForFile(Long fileId) {
        sourceFiles.getById(fileId);
        return explanations.findBySourceFileIdOrderByCreatedAtDesc(fileId);
    }

    @Transactional(readOnly = true)
    public Explanation getById(Long id) {
        return explanations.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("There is no explanation with id " + id + "."));
    }

    @Transactional
    public Explanation edit(Long id, String content) {
        Explanation explanation = getById(id);
        explanation.setContent(content);
        // Any edit means the AI's draft has changed, so it needs approving again.
        explanation.setStatus(STATUS_DRAFT);
        explanation.setApprovedBy(null);
        explanation.setApprovedAt(null);
        return explanations.save(explanation);
    }

    @Transactional
    public Explanation approve(Long id, Long approvedBy) {
        Explanation explanation = getById(id);
        explanation.setStatus(STATUS_APPROVED);
        explanation.setApprovedBy(approvedBy);
        explanation.setApprovedAt(OffsetDateTime.now());
        return explanations.save(explanation);
    }

    private String activeModel() {
        return "openai".equals(aiProperties.provider())
            ? aiProperties.openai().model()
            : aiProperties.claude().model();
    }

    /** Reads the file text, optionally just a line range, and caps the total size. */
    private String readCode(SourceFile file, Integer startLine, Integer endLine) {
        int start = (startLine == null || startLine < 1) ? 1 : startLine;
        int end = (endLine == null) ? Integer.MAX_VALUE : endLine;
        if (end < start) {
            throw new BadRequestException("The end line cannot be before the start line.");
        }

        StringBuilder code = new StringBuilder();
        try (InputStream in = sourceFiles.openContent(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            int number = 0;
            while ((line = reader.readLine()) != null) {
                number++;
                if (number < start) {
                    continue;
                }
                if (number > end) {
                    break;
                }
                code.append(line).append('\n');
                if (code.length() > MAX_CODE_CHARS) {
                    code.append("... (the rest was left out because the file is large)\n");
                    break;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the file to explain it.", e);
        }
        return code.toString();
    }
}
