package com.corewise.modernization.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares the COBOL output (the golden answer) with the Java output.
 *
 * <p>The comparison options are always explicit and always reported, never a silent
 * guess:
 * <ul>
 *   <li>Trim trailing spaces: COBOL often pads lines with spaces, so we can trim the
 *       trailing spaces on each line before comparing.
 *   <li>Numeric tolerance: numbers can format a little differently, for example 250
 *       versus 250.00, so we can allow a small numeric difference.
 * </ul>
 *
 * <p>The result also says which kind of difference we saw, so a difference that is only
 * formatting is never confused with a real behavior difference:
 * <ul>
 *   <li>NONE: the outputs matched exactly.
 *   <li>FORMATTING: they matched only after trimming spaces or allowing the tolerance.
 *       This still passes, but we flag it.
 *   <li>BEHAVIOR: they still differ. This fails, and we show where.
 * </ul>
 */
public final class OutputComparator {

  private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?");

  public enum DifferenceKind {
    NONE,
    FORMATTING,
    BEHAVIOR
  }

  public record ComparisonResult(boolean passed, DifferenceKind kind, String diff) {
  }

  private OutputComparator() {
  }

  public static ComparisonResult compare(String cobolOutput, String javaOutput,
      boolean trimTrailingSpace, double numericTolerance) {
    String cobol = cobolOutput == null ? "" : cobolOutput;
    String java = javaOutput == null ? "" : javaOutput;

    // Exact match, nothing to explain.
    if (cobol.equals(java)) {
      return new ComparisonResult(true, DifferenceKind.NONE, "");
    }

    List<String> cobolLines = normalize(cobol, trimTrailingSpace);
    List<String> javaLines = normalize(java, trimTrailingSpace);

    if (linesMatch(cobolLines, javaLines, numericTolerance)) {
      // They only matched once we applied the chosen settings, so it is formatting.
      return new ComparisonResult(true, DifferenceKind.FORMATTING, "");
    }

    return new ComparisonResult(false, DifferenceKind.BEHAVIOR, buildDiff(cobolLines, javaLines));
  }

  /** Normalize line endings, optionally trim trailing spaces, and drop trailing blank lines. */
  private static List<String> normalize(String text, boolean trimTrailingSpace) {
    String unified = text.replace("\r\n", "\n").replace('\r', '\n');
    List<String> lines = new ArrayList<>(List.of(unified.split("\n", -1)));
    if (trimTrailingSpace) {
      for (int i = 0; i < lines.size(); i++) {
        lines.set(i, stripTrailing(lines.get(i)));
      }
    }
    // A trailing newline should not count as a difference.
    while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
      lines.remove(lines.size() - 1);
    }
    return lines;
  }

  private static boolean linesMatch(List<String> cobol, List<String> java, double tolerance) {
    if (cobol.size() != java.size()) {
      return false;
    }
    for (int i = 0; i < cobol.size(); i++) {
      if (!lineMatches(cobol.get(i), java.get(i), tolerance)) {
        return false;
      }
    }
    return true;
  }

  /** A line matches if it is equal, or if only its numbers differ within the tolerance. */
  private static boolean lineMatches(String cobol, String java, double tolerance) {
    if (cobol.equals(java)) {
      return true;
    }
    if (tolerance <= 0) {
      return false;
    }
    // Compare the non-numeric skeleton exactly, and each number within the tolerance.
    String cobolSkeleton = NUMBER.matcher(cobol).replaceAll("#");
    String javaSkeleton = NUMBER.matcher(java).replaceAll("#");
    if (!cobolSkeleton.equals(javaSkeleton)) {
      return false;
    }
    List<Double> cobolNumbers = numbersIn(cobol);
    List<Double> javaNumbers = numbersIn(java);
    if (cobolNumbers.size() != javaNumbers.size()) {
      return false;
    }
    for (int i = 0; i < cobolNumbers.size(); i++) {
      if (Math.abs(cobolNumbers.get(i) - javaNumbers.get(i)) > tolerance) {
        return false;
      }
    }
    return true;
  }

  private static List<Double> numbersIn(String line) {
    List<Double> numbers = new ArrayList<>();
    Matcher matcher = NUMBER.matcher(line);
    while (matcher.find()) {
      numbers.add(Double.parseDouble(matcher.group()));
    }
    return numbers;
  }

  /** A plain, line by line diff that points at exactly where the two outputs differ. */
  private static String buildDiff(List<String> cobol, List<String> java) {
    StringBuilder diff = new StringBuilder();
    int max = Math.max(cobol.size(), java.size());
    int shown = 0;
    for (int i = 0; i < max; i++) {
      String cobolLine = i < cobol.size() ? cobol.get(i) : null;
      String javaLine = i < java.size() ? java.get(i) : null;
      if (Objects.equals(cobolLine, javaLine)) {
        continue;
      }
      if (shown >= 200) {
        diff.append("... more differences not shown ...\n");
        break;
      }
      shown++;
      diff.append("Line ").append(i + 1).append(":\n");
      diff.append("  COBOL: ").append(cobolLine == null ? "(no line)" : cobolLine).append('\n');
      diff.append("  Java:  ").append(javaLine == null ? "(no line)" : javaLine).append('\n');
    }
    return diff.toString();
  }

  private static String stripTrailing(String line) {
    int end = line.length();
    while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
      end--;
    }
    return line.substring(0, end);
  }
}
