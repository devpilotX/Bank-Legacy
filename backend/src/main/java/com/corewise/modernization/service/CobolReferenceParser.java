package com.corewise.modernization.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds the obvious links in COBOL source: which programs it calls and which
 * copybooks it pulls in. This is the easy, reliable half of the map. The trickier
 * links (indirect calls, data flow) are left to the AI, which marks them for a human
 * to confirm.
 *
 * We skip comment lines so a CALL or COPY written inside a comment does not count.
 */
public final class CobolReferenceParser {

    private static final Pattern CALL =
        Pattern.compile("(?i)\\bCALL\\s+['\"]([A-Za-z0-9_-]+)['\"]");
    private static final Pattern COPY =
        Pattern.compile("(?i)\\bCOPY\\s+([A-Za-z0-9_-]+)");

    private CobolReferenceParser() {
    }

    public static References parse(String content) {
        Set<String> calls = new LinkedHashSet<>();
        Set<String> copies = new LinkedHashSet<>();
        if (content == null || content.isBlank()) {
            return new References(calls, copies);
        }
        for (String line : content.split("\\r?\\n")) {
            if (isComment(line)) {
                continue;
            }
            Matcher callMatcher = CALL.matcher(line);
            while (callMatcher.find()) {
                calls.add(callMatcher.group(1).toUpperCase());
            }
            Matcher copyMatcher = COPY.matcher(line);
            while (copyMatcher.find()) {
                copies.add(copyMatcher.group(1).toUpperCase());
            }
        }
        return new References(calls, copies);
    }

    private static boolean isComment(String line) {
        String trimmed = line.stripLeading();
        if (trimmed.startsWith("*")) {
            return true;
        }
        // Fixed-format COBOL marks a comment with a * (or /) in column 7.
        return line.length() > 6 && (line.charAt(6) == '*' || line.charAt(6) == '/');
    }

    /** The program names called and the copybook names pulled in. */
    public record References(Set<String> calls, Set<String> copies) {
    }
}
