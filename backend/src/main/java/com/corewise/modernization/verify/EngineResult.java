package com.corewise.modernization.verify;

import com.corewise.modernization.verify.OutputComparator.DifferenceKind;
import java.util.Locale;

/**
 * The full result of checking one case: what each side produced, whether they matched,
 * the kind of difference, a readable diff when they differ, a plain message, and the
 * comparison settings that were used.
 */
public record EngineResult(
    Outcome outcome,
    boolean passed,
    String cobolOutput,
    String javaOutput,
    String diff,
    DifferenceKind differenceKind,
    String detail,
    boolean trimmedTrailingSpace,
    double numericTolerance) {

  /** The plain result of a run, including the error paths. */
  public enum Outcome {
    PASSED,
    FAILED,
    COBOL_COMPILE_ERROR,
    COBOL_RUN_ERROR,
    JAVA_COMPILE_ERROR,
    JAVA_RUN_ERROR,
    TIMEOUT,
    ENGINE_ERROR;

    /** The value stored in the database, matching the migration's allowed values. */
    public String dbValue() {
      return name().toLowerCase(Locale.ROOT);
    }
  }
}
