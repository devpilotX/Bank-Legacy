package com.corewise.modernization.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.corewise.modernization.verify.EngineResult.Outcome;
import com.corewise.modernization.verify.OutputComparator.DifferenceKind;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The whole engine on real COBOL and real Java. This needs GnuCOBOL installed. It looks
 * for it in the verify.gnucobol.home system property, then the VERIFY_GNUCOBOL_HOME
 * environment variable, then the usual install spot on this machine. If it cannot find
 * cobc, the test is skipped, not failed, so a machine without GnuCOBOL still builds.
 * Where GnuCOBOL is present, these run for real and the build fails if they fail.
 */
class VerificationEngineCobolTest {

  // Reads a principal from standard input, works out 0.25 percent interest, and prints
  // it zero padded. Plain fixed format COBOL, columns laid out by hand.
  private static final String COBOL = String.join("\n",
      "       IDENTIFICATION DIVISION.",
      "       PROGRAM-ID. INTEREST.",
      "       DATA DIVISION.",
      "       WORKING-STORAGE SECTION.",
      "       01  WS-PRINCIPAL  PIC 9(7).",
      "       01  WS-INTEREST   PIC 9(6).",
      "       PROCEDURE DIVISION.",
      "       MAIN-PARA.",
      "           ACCEPT WS-PRINCIPAL",
      "           COMPUTE WS-INTEREST = WS-PRINCIPAL * 0.0025",
      "           DISPLAY \"INTEREST=\" WS-INTEREST",
      "           STOP RUN.");

  private static final String CORRECT_JAVA = """
      import java.util.Scanner;

      public class Interest {
        public static void main(String[] args) {
          Scanner scanner = new Scanner(System.in);
          int principal = scanner.nextInt();
          int interest = principal * 25 / 10000;
          System.out.printf("INTEREST=%06d%n", interest);
        }
      }""";

  // The same, but the rate is ten times too big. The engine should catch this.
  private static final String BUGGY_JAVA = """
      import java.util.Scanner;

      public class Interest {
        public static void main(String[] args) {
          Scanner scanner = new Scanner(System.in);
          int principal = scanner.nextInt();
          int interest = principal * 25 / 1000;
          System.out.printf("INTEREST=%06d%n", interest);
        }
      }""";

  private String gnucobolHome() {
    String home = System.getProperty("verify.gnucobol.home");
    if (home == null || home.isBlank()) {
      home = System.getenv("VERIFY_GNUCOBOL_HOME");
    }
    if (home == null || home.isBlank()) {
      home = "C:\\Users\\Dipan\\tools\\gnucobol";
    }
    return home;
  }

  private VerificationEngine engine() {
    VerifyProperties props = new VerifyProperties(true, gnucobolHome(), "cobc", "javac", "java",
        Duration.ofSeconds(60), Duration.ofSeconds(15), null, true, 0.0);
    Sandbox sandbox = new Sandbox();
    return new VerificationEngine(props, new CobolRunner(props, sandbox),
        new JavaRunner(props, sandbox));
  }

  @BeforeEach
  void requireGnuCobol() {
    Path cobc = Paths.get(gnucobolHome(), "bin", "cobc.exe");
    assumeTrue(Files.exists(cobc),
        "GnuCOBOL not found, skipping. Set verify.gnucobol.home to run this test.");
  }

  @Test
  void correctJavaMatchesTheCobol() {
    EngineResult result = engine().run(COBOL, CORRECT_JAVA, "100000", Map.of(), true, 0);
    assertEquals(Outcome.PASSED, result.outcome(),
        () -> "detail: " + result.detail()
            + " cobol=[" + result.cobolOutput() + "] java=[" + result.javaOutput() + "]");
    assertTrue(result.passed());
    assertTrue(result.cobolOutput().contains("000250"));
  }

  @Test
  void buggyJavaIsCaughtWithADiff() {
    EngineResult result = engine().run(COBOL, BUGGY_JAVA, "100000", Map.of(), true, 0);
    assertEquals(Outcome.FAILED, result.outcome(),
        () -> "detail: " + result.detail()
            + " cobol=[" + result.cobolOutput() + "] java=[" + result.javaOutput() + "]");
    assertFalse(result.passed());
    assertEquals(DifferenceKind.BEHAVIOR, result.differenceKind());
    assertFalse(result.diff().isBlank());
  }
}
