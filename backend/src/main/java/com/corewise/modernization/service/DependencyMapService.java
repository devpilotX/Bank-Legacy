package com.corewise.modernization.service;

import com.corewise.modernization.ai.AiClient;
import com.corewise.modernization.ai.AiException;
import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.DependencyEdge;
import com.corewise.modernization.domain.model.DependencyNode;
import com.corewise.modernization.domain.model.SourceFile;
import com.corewise.modernization.repository.DependencyEdgeRepository;
import com.corewise.modernization.repository.DependencyNodeRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the picture of how an old system fits together. Parsing finds the obvious
 * links (CALL and COPY) and marks them confirmed, since they are facts in the code.
 * The AI can add trickier links, which are saved as suggestions for a person to
 * confirm or reject. A rebuild recomputes the parsed links but leaves the AI links
 * and any human decisions on them in place.
 */
@Service
public class DependencyMapService {

    private static final Logger log = LoggerFactory.getLogger(DependencyMapService.class);
    private static final int MAX_CONTENT_CHARS = 2_000_000;

    private static final String ORIGIN_PARSED = "parsed";
    private static final String ORIGIN_AI = "ai";
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_SUGGESTED = "suggested";
    private static final String STATUS_REJECTED = "rejected";

    private static final String AI_SYSTEM_PROMPT = """
        You map dependencies in old mainframe systems. Given one source file, list links
        that simple CALL and COPY parsing would miss, such as indirect calls or data files
        the program reads or writes. Reply with one link per line in the form
        TARGET|KIND|why, where KIND is one of calls, includes, reads, writes, relates. If
        you are not sure, leave it out. Reply with nothing else.""";

    private final DependencyNodeRepository nodes;
    private final DependencyEdgeRepository edges;
    private final ProjectService projects;
    private final SourceFileService sourceFiles;
    private final AiClient aiClient;

    public DependencyMapService(DependencyNodeRepository nodes, DependencyEdgeRepository edges,
                                ProjectService projects, SourceFileService sourceFiles,
                                AiClient aiClient) {
        this.nodes = nodes;
        this.edges = edges;
        this.projects = projects;
        this.sourceFiles = sourceFiles;
        this.aiClient = aiClient;
    }

    @Transactional
    public DependencyMap buildOrRefresh(Long projectId, boolean includeAi) {
        projects.getById(projectId);
        List<SourceFile> files = sourceFiles.list(projectId);

        // Parsed links are recomputed from scratch each time so removed calls disappear.
        edges.deleteByProjectIdAndOrigin(projectId, ORIGIN_PARSED);

        for (SourceFile file : files) {
            DependencyNode fileNode = ensureNode(projectId, file.getFilename(), nodeKind(file), file.getId());
            String content = readContent(file);
            CobolReferenceParser.References refs = CobolReferenceParser.parse(content);
            for (String callee : refs.calls()) {
                DependencyNode target = ensureNode(projectId, callee, "program", null);
                upsertParsedEdge(projectId, fileNode, target, "calls", "CALL " + callee);
            }
            for (String copy : refs.copies()) {
                DependencyNode target = ensureNode(projectId, copy, "copybook", null);
                upsertParsedEdge(projectId, fileNode, target, "includes", "COPY " + copy);
            }
        }

        if (includeAi) {
            addAiSuggestions(projectId, files);
        }
        return getMap(projectId);
    }

    @Transactional(readOnly = true)
    public DependencyMap getMap(Long projectId) {
        projects.getById(projectId);
        return new DependencyMap(nodes.findByProjectIdOrderByNameAsc(projectId), edges.findByProjectId(projectId));
    }

    @Transactional
    public DependencyEdge confirmLink(Long edgeId) {
        return decideLink(edgeId, STATUS_CONFIRMED);
    }

    @Transactional
    public DependencyEdge rejectLink(Long edgeId) {
        return decideLink(edgeId, STATUS_REJECTED);
    }

    private DependencyEdge decideLink(Long edgeId, String status) {
        DependencyEdge edge = edges.findById(edgeId)
            .orElseThrow(() -> new ResourceNotFoundException("There is no dependency link with id " + edgeId + "."));
        if (!ORIGIN_AI.equals(edge.getOrigin())) {
            throw new BadRequestException("Only AI-suggested links can be confirmed or rejected. "
                + "Parsed links come straight from the code.");
        }
        edge.setStatus(status);
        return edges.save(edge);
    }

    private void addAiSuggestions(Long projectId, List<SourceFile> files) {
        for (SourceFile file : files) {
            if (!"cobol".equals(file.getLanguage())) {
                continue;
            }
            try {
                DependencyNode fileNode = ensureNode(projectId, file.getFilename(), nodeKind(file), file.getId());
                String answer = aiClient.complete(AI_SYSTEM_PROMPT,
                    "File: " + file.getFilename() + "\n\n" + readContent(file));
                for (String line : answer.split("\\r?\\n")) {
                    parseAiLink(projectId, fileNode, line);
                }
            } catch (AiException e) {
                // The AI is best-effort here. If it is down or not set up, we keep the
                // parsed map and note why, rather than failing the whole build.
                log.warn("Skipped AI suggestions for {} in project {}: {}",
                    file.getFilename(), projectId, e.getMessage());
                return;
            }
        }
    }

    private void parseAiLink(Long projectId, DependencyNode fileNode, String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 2) {
            return;
        }
        String targetName = parts[0].trim().toUpperCase();
        String kind = parts[1].trim().toLowerCase();
        String detail = parts.length >= 3 ? parts[2].trim() : null;
        if (targetName.isEmpty() || !List.of("calls", "includes", "reads", "writes", "relates").contains(kind)) {
            return;
        }
        DependencyNode target = ensureNode(projectId, targetName, "unknown", null);
        // Only add a suggestion if we do not already have this exact link.
        if (edges.findFirstByProjectIdAndFromNodeIdAndToNodeIdAndKind(
                projectId, fileNode.getId(), target.getId(), kind).isEmpty()) {
            edges.save(new DependencyEdge(projectId, fileNode.getId(), target.getId(),
                kind, ORIGIN_AI, STATUS_SUGGESTED, detail));
        }
    }

    private DependencyNode ensureNode(Long projectId, String name, String kind, Long sourceFileId) {
        return nodes.findByProjectIdAndNameAndKind(projectId, name, kind)
            .orElseGet(() -> nodes.save(new DependencyNode(projectId, name, kind, sourceFileId)));
    }

    private void upsertParsedEdge(Long projectId, DependencyNode from, DependencyNode to,
                                  String kind, String detail) {
        DependencyEdge edge = edges.findFirstByProjectIdAndFromNodeIdAndToNodeIdAndKind(
            projectId, from.getId(), to.getId(), kind).orElse(null);
        if (edge == null) {
            edges.save(new DependencyEdge(projectId, from.getId(), to.getId(),
                kind, ORIGIN_PARSED, STATUS_CONFIRMED, detail));
        } else {
            // Parsing found a link the AI had only guessed at, so confirm it as a fact.
            edge.setStatus(STATUS_CONFIRMED);
            edges.save(edge);
        }
    }

    private String nodeKind(SourceFile file) {
        return switch (file.getLanguage()) {
            case "cobol" -> "program";
            case "copybook" -> "copybook";
            case "jcl" -> "file";
            default -> "unknown";
        };
    }

    private String readContent(SourceFile file) {
        StringBuilder content = new StringBuilder();
        try (InputStream in = sourceFiles.openContent(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
                if (content.length() > MAX_CONTENT_CHARS) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read a source file while building the map.", e);
        }
        return content.toString();
    }

    /** The whole map for a project: its parts and the links between them. */
    public record DependencyMap(List<DependencyNode> nodes, List<DependencyEdge> edges) {
    }
}
