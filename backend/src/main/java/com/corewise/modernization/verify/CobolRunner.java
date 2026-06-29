package com.corewise.modernization.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Compiles and runs the original COBOL so we can capture the true expected output, the
 * golden answer. Compiling and running both happen in the sandbox, in a throwaway folder
 * that is deleted afterward.
 *
 * <p>GnuCOBOL turns COBOL into C and then calls a C compiler, so the child process needs
 * the GnuCOBOL bin folder (which holds cobc, the bundled gcc, and the runtime libraries)
 * on its PATH, plus the COB_* variables that tell cobc where its config and includes are.
 * We set all of that from config, so nothing about this machine is hard coded.
 */
@Component
public class CobolRunner {

  private final VerifyProperties props;
  private final Sandbox sandbox;

  public CobolRunner(VerifyProperties props, Sandbox sandbox) {
    this.props = props;
    this.sandbox = sandbox;
  }

  /**
   * Compile the given COBOL and run it on the given input. Any input files (name to
   * content) are written into the working folder first, for programs that read files
   * instead of standard input.
   */
  public ProgramResult run(String cobolSource, String stdin, Map<String, String> inputFiles) {
    Path work = null;
    try {
      work = sandbox.createWorkDir("verify-cobol");
      Files.writeString(work.resolve("program.cob"), cobolSource, StandardCharsets.UTF_8);
      writeInputFiles(work, inputFiles);

      Map<String, String> env = cobolEnv();

      // Compile to a standalone executable. -x means "make a program I can run".
      Sandbox.Outcome compile = sandbox.run(
          List.of(cobcTool(), "-x", "-o", "program.exe", "program.cob"),
          work, "", env, props.compileTimeout());
      if (compile.timedOut()) {
        return ProgramResult.timedOut("Compiling the COBOL took too long and was stopped.");
      }
      if (compile.exitCode() != 0) {
        return ProgramResult.compileFailed(firstNonBlank(compile.stderr(), compile.stdout(),
            "The COBOL did not compile."));
      }
      Path exe = work.resolve("program.exe");
      if (!Files.exists(exe)) {
        return ProgramResult.compileFailed(
            "The COBOL compiled but produced no program to run.");
      }

      // Run it, feeding the case input on standard input.
      Sandbox.Outcome runOutcome = sandbox.run(
          List.of(exe.toString()), work, stdin == null ? "" : stdin, env, props.runTimeout());
      if (runOutcome.timedOut()) {
        return ProgramResult.timedOut("The COBOL program ran too long and was stopped.");
      }
      if (runOutcome.exitCode() != 0) {
        return ProgramResult.runFailed(firstNonBlank(runOutcome.stderr(),
            "The COBOL program stopped with an error (exit code " + runOutcome.exitCode() + ")."));
      }
      return ProgramResult.ranOk(runOutcome.stdout());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ProgramResult.runFailed("Running the COBOL was interrupted.");
    } catch (IOException e) {
      return ProgramResult.runFailed("The engine could not run the COBOL: " + e.getMessage());
    } finally {
      sandbox.cleanUp(work);
    }
  }

  private void writeInputFiles(Path work, Map<String, String> inputFiles) throws IOException {
    if (inputFiles == null) {
      return;
    }
    for (Map.Entry<String, String> file : inputFiles.entrySet()) {
      // Keep file names simple and inside the work folder; ignore anything with a path.
      String name = file.getKey();
      if (name == null || name.isBlank() || name.contains("/") || name.contains("\\")) {
        continue;
      }
      Files.writeString(work.resolve(name), file.getValue() == null ? "" : file.getValue(),
          StandardCharsets.UTF_8);
    }
  }

  /** The cobc to call. With a configured home we use its bin so PATH lookup is not needed. */
  private String cobcTool() {
    String home = props.gnucobolHome();
    if (home == null || home.isBlank()) {
      return props.cobcCommand();
    }
    String exe = props.cobcCommand();
    if (isWindows() && !exe.toLowerCase().endsWith(".exe")) {
      exe = exe + ".exe";
    }
    return home + File.separator + "bin" + File.separator + exe;
  }

  /** The environment cobc needs: its bin on PATH, plus where its config and includes live. */
  private Map<String, String> cobolEnv() {
    Map<String, String> env = new HashMap<>();
    String home = props.gnucobolHome();
    if (home == null || home.isBlank()) {
      return env;
    }
    String bin = home + File.separator + "bin";
    String parentPath = System.getenv("PATH");
    env.put("PATH", bin + File.pathSeparator + (parentPath == null ? "" : parentPath));
    env.put("COB_CONFIG_DIR", home + File.separator + "config");
    env.put("COB_COPY_DIR", home + File.separator + "copy");
    env.put("COB_CFLAGS", "-I\"" + home + File.separator + "include\"");
    env.put("COB_LDFLAGS", "-L\"" + home + File.separator + "lib\"");
    env.put("COB_LIBRARY_PATH", home + File.separator + "extras");
    return env;
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
