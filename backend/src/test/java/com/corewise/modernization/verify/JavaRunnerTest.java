package com.corewise.modernization.verify;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * The Java side of the engine, run for real with the JDK the build uses. This covers the
 * compile error path and the timeout path on their own, with no COBOL needed, so they
 * always run and the build fails if they fail.
 */
class JavaRunnerTest {

  private JavaRunner runner(Duration runTimeout) {
    VerifyProperties props = new VerifyProperties(true, null, "cobc", "javac", "java",
        Duration.ofSeconds(60), runTimeout, null, true, 0.0);
    return new JavaRunner(props, new Sandbox());
  }

  @Test
  void correctJavaCompilesRunsAndReturnsOutput() {
    String source = """
        public class Echo {
          public static void main(String[] args) throws Exception {
            var reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            System.out.println("GOT=" + reader.readLine());
          }
        }""";
    ProgramResult result = runner(Duration.ofSeconds(20)).run(source, "hello");
    assertTrue(result.compiled(), "should compile");
    assertTrue(result.ran(), "should run");
    assertTrue(result.output().contains("GOT=hello"), () -> "output was: " + result.output());
  }

  @Test
  void brokenJavaReportsACompileError() {
    String source = "public class Broken { this is not valid java }";
    ProgramResult result = runner(Duration.ofSeconds(20)).run(source, "");
    assertFalse(result.compiled(), "should not compile");
    assertFalse(result.detail().isBlank(), "should explain the compile error");
  }

  @Test
  void anEndlessProgramTimesOut() {
    String source = """
        public class Loop {
          public static void main(String[] args) {
            while (true) {
              // spin forever on purpose
            }
          }
        }""";
    ProgramResult result = runner(Duration.ofSeconds(2)).run(source, "");
    assertTrue(result.timedOut(), "should be stopped for running too long");
  }
}
