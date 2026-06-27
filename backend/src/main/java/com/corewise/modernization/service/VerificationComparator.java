package com.corewise.modernization.service;

/**
 * Decides whether an actual output matches the expected one. We compare leniently on
 * spacing: line endings are normalized, trailing spaces on each line are dropped, and
 * blank lines at the very start and end are ignored. Everything else must match
 * exactly, because in banking a difference in a number is a real difference.
 */
public final class VerificationComparator {

    private VerificationComparator() {
    }

    public static Result evaluate(String expected, String actual) {
        String[] expectedLines = normalize(expected);
        String[] actualLines = normalize(actual);

        if (expectedLines.length != actualLines.length) {
            return new Result(false, "The output had " + actualLines.length
                + " line(s) but " + expectedLines.length + " were expected.");
        }
        for (int i = 0; i < expectedLines.length; i++) {
            if (!expectedLines[i].equals(actualLines[i])) {
                return new Result(false, "Line " + (i + 1) + " did not match. Expected \""
                    + trim(expectedLines[i]) + "\" but got \"" + trim(actualLines[i]) + "\".");
            }
        }
        return new Result(true, "The output matched the expected result.");
    }

    private static String[] normalize(String text) {
        if (text == null) {
            return new String[0];
        }
        String unified = text.replace("\r\n", "\n").replace('\r', '\n').strip();
        if (unified.isEmpty()) {
            return new String[0];
        }
        String[] lines = unified.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            lines[i] = stripTrailing(lines[i]);
        }
        return lines;
    }

    private static String stripTrailing(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }

    private static String trim(String value) {
        return value.length() <= 80 ? value : value.substring(0, 77) + "...";
    }

    /** Whether the run passed, and a plain-English reason. */
    public record Result(boolean passed, String detail) {
    }
}
