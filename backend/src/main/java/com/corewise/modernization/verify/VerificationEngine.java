package com.corewise.modernization.verify;

import com.corewise.modernization.verify.EngineResult.Outcome;
import com.corewise.modernization.verify.OutputComparator.ComparisonResult;
import com.corewise.modernization.verify.OutputComparator.DifferenceKind;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The verification engine. For one case it runs the original COBOL to get the true
 * expected output, runs the new Java on the same input, and compares the two. It returns
 * a full result, including the clear error paths: a side that does not compile, a side
 * that fails at run time, and a run that takes too long.
 */
@Component
public class VerificationEngine {

  private final VerifyProperties props;
  private final CobolRunner cobolRunner;
  private final JavaRunner javaRunner;

  public VerificationEngine(VerifyProperties props, CobolRunner cobolRunner,
      JavaRunner javaRunner) {
    this.props = props;
    this.cobolRunner = cobolRunner;
    this.javaRunner = javaRunner;
  }

  public boolean isEnabled() {
    return Boolean.TRUE.equals(props.enabled());
  }

  /** Run one case end to end and return the full result. */
  public EngineResult run(String cobolSource, String javaSource, String stdin,
      Map<String, String> inputFiles, boolean trimTrailingSpace, double numericTolerance) {

    if (!isEnabled()) {
      return engineError("The verification engine is turned off in this environment.",
          trimTrailingSpace, numericTolerance);
    }
    if (isBlank(cobolSource)) {
      return engineError("There is no original COBOL to run for this unit.",
          trimTrailingSpace, numericTolerance);
    }
    if (isBlank(javaSource)) {
      return engineError("There is no approved Java to check yet. Save the Java first.",
          trimTrailingSpace, numericTolerance);
    }

    // 1. Run the old COBOL to get the golden answer.
    ProgramResult cobol = cobolRunner.run(cobolSource, stdin, inputFiles);
    if (cobol.timedOut()) {
      return result(Outcome.TIMEOUT, false, "", "", "", null, cobol.detail(),
          trimTrailingSpace, numericTolerance);
    }
    if (!cobol.compiled()) {
      return result(Outcome.COBOL_COMPILE_ERROR, false, "", "", "", null,
          "The original COBOL did not compile:\n" + cobol.detail(),
          trimTrailingSpace, numericTolerance);
    }
    if (!cobol.ran()) {
      return result(Outcome.COBOL_RUN_ERROR, false, "", "", "", null,
          "The original COBOL did not run cleanly:\n" + cobol.detail(),
          trimTrailingSpace, numericTolerance);
    }
    String golden = cobol.output();

    // 2. Run the new Java on the same input.
    ProgramResult java = javaRunner.run(javaSource, stdin);
    if (java.timedOut()) {
      return result(Outcome.TIMEOUT, false, golden, "", "", null, java.detail(),
          trimTrailingSpace, numericTolerance);
    }
    if (!java.compiled()) {
      return result(Outcome.JAVA_COMPILE_ERROR, false, golden, "", "", null,
          "The new Java did not compile:\n" + java.detail(),
          trimTrailingSpace, numericTolerance);
    }
    if (!java.ran()) {
      return result(Outcome.JAVA_RUN_ERROR, false, golden, "", "", null,
          "The new Java did not run cleanly:\n" + java.detail(),
          trimTrailingSpace, numericTolerance);
    }
    String actual = java.output();

    // 3. Compare the two outputs.
    ComparisonResult comparison =
        OutputComparator.compare(golden, actual, trimTrailingSpace, numericTolerance);
    Outcome outcome = comparison.passed() ? Outcome.PASSED : Outcome.FAILED;
    return result(outcome, comparison.passed(), golden, actual, comparison.diff(),
        comparison.kind(), message(comparison, trimTrailingSpace, numericTolerance),
        trimTrailingSpace, numericTolerance);
  }

  /** A warm, plain message that fits the result. */
  private static String message(ComparisonResult comparison, boolean trim, double tolerance) {
    return switch (comparison.kind()) {
      case NONE -> "The Java produced the same output as the COBOL.";
      case FORMATTING -> {
        StringBuilder note = new StringBuilder("The Java matched the COBOL");
        if (trim && tolerance > 0) {
          note.append(" after trimming trailing spaces and allowing a small numeric difference.");
        } else if (trim) {
          note.append(" after trimming trailing spaces.");
        } else if (tolerance > 0) {
          note.append(" after allowing a small numeric difference.");
        } else {
          note.append(".");
        }
        note.append(" The only difference was formatting.");
        yield note.toString();
      }
      case BEHAVIOR ->
          "The Java gave a different answer than the COBOL on this input. Here is what differs.";
    };
  }

  private static EngineResult engineError(String detail, boolean trim, double tolerance) {
    return result(Outcome.ENGINE_ERROR, false, "", "", "", null, detail, trim, tolerance);
  }

  private static EngineResult result(Outcome outcome, boolean passed, String cobolOutput,
      String javaOutput, String diff, DifferenceKind kind, String detail,
      boolean trim, double tolerance) {
    return new EngineResult(outcome, passed, cobolOutput, javaOutput, diff, kind, detail,
        trim, tolerance);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
