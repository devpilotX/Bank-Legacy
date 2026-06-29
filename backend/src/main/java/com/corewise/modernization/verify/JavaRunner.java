package com.corewise.modernization.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Compiles and runs the approved Java the same way we run the COBOL, in the sandbox, so
 * we can compare what it produces. We resolve javac and java from the JDK this app is
 * running on, so the engine does not depend on the PATH being set just so.
 */
@Component
public class JavaRunner {

  // Find the class we should name the file after. Prefer a public class.
  private static final Pattern PUBLIC_CLASS =
      Pattern.compile("public\\s+(?:final\\s+|abstract\\s+)?class\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
  private static final Pattern ANY_CLASS =
      Pattern.compile("\\bclass\\s+([A-Za-z_$][A-Za-z0-9_$]*)");

  private final VerifyProperties props;
  private final Sandbox sandbox;

  public JavaRunner(VerifyProperties props, Sandbox sandbox) {
    this.props = props;
    this.sandbox = sandbox;
  }

  /** Compile the given Java and run it on the given standard input. */
  public ProgramResult run(String javaSource, String stdin) {
    Path work = null;
    try {
      work = sandbox.createWorkDir("verify-java");
      String source = stripCodeFences(javaSource);
      String className = detectClassName(source);
      Files.writeString(work.resolve(className + ".java"), source, StandardCharsets.UTF_8);

      Sandbox.Outcome compile = sandbox.run(
          List.of(tool(props.javacCommand(), "javac"), "-d", ".", className + ".java"),
          work, "", Map.of(), props.compileTimeout());
      if (compile.timedOut()) {
        return ProgramResult.timedOut("Compiling the Java took too long and was stopped.");
      }
      if (compile.exitCode() != 0) {
        return ProgramResult.compileFailed(firstNonBlank(compile.stderr(), compile.stdout(),
            "The Java did not compile."));
      }

      Sandbox.Outcome runOutcome = sandbox.run(
          List.of(tool(props.javaCommand(), "java"), "-cp", ".", className),
          work, stdin == null ? "" : stdin, Map.of(), props.runTimeout());
      if (runOutcome.timedOut()) {
        return ProgramResult.timedOut("The Java program ran too long and was stopped.");
      }
      if (runOutcome.exitCode() != 0) {
        return ProgramResult.runFailed(firstNonBlank(runOutcome.stderr(),
            "The Java program stopped with an error (exit code " + runOutcome.exitCode() + ")."));
      }
      return ProgramResult.ranOk(runOutcome.stdout());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ProgramResult.runFailed("Running the Java was interrupted.");
    } catch (IOException e) {
      return ProgramResult.runFailed("The engine could not run the Java: " + e.getMessage());
    } finally {
      sandbox.cleanUp(work);
    }
  }

  /**
   * Engineers paste reviewed Java, but a first draft from the AI sometimes still has
   * markdown code fences around it. Strip those lines so it still compiles.
   */
  static String stripCodeFences(String source) {
    if (source == null) {
      return "";
    }
    StringBuilder cleaned = new StringBuilder();
    for (String line : source.split("\n", -1)) {
      if (line.strip().startsWith("```")) {
        continue;
      }
      cleaned.append(line).append('\n');
    }
    return cleaned.toString();
  }

  /** Work out the class name to use for the file. Falls back to Main. */
  static String detectClassName(String source) {
    Matcher pub = PUBLIC_CLASS.matcher(source);
    if (pub.find()) {
      return pub.group(1);
    }
    Matcher any = ANY_CLASS.matcher(source);
    if (any.find()) {
      return any.group(1);
    }
    return "Main";
  }

  /** Use the JDK this app runs on for javac and java, unless config overrode the command. */
  private String tool(String configured, String defaultName) {
    if (configured != null && !configured.equals(defaultName)) {
      return configured;
    }
    String javaHome = System.getProperty("java.home");
    if (javaHome != null && !javaHome.isBlank()) {
      String exe = isWindows() ? defaultName + ".exe" : defaultName;
      Path candidate = Paths.get(javaHome, "bin", exe);
      if (Files.exists(candidate)) {
        return candidate.toString();
      }
    }
    return configured;
  }

  private static boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase().contains("win");
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value.strip();
      }
    }
    return "";
  }
}
