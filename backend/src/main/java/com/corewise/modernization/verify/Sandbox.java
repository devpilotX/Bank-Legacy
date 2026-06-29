package com.corewise.modernization.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Runs one external command in its own process, inside a throwaway working folder, with
 * a hard timeout. This is how we safely run code we generated.
 *
 * <p>Why it is built this way:
 * <ul>
 *   <li>Separate process: the compiled COBOL or Java never runs inside our app, so a
 *       crash or a runaway loop cannot take the app down with it.
 *   <li>Hard timeout: if a program runs too long we kill it, so a bad program cannot
 *       hang the system.
 *   <li>Throwaway folder: each run gets a clean temp folder that the caller deletes
 *       afterward, so runs do not see each other's files.
 *   <li>Redirected input and output go to files, not pipes, so a chatty program can
 *       never deadlock by filling a pipe buffer while we wait on another one.
 * </ul>
 *
 * <p>Honest limit: this is process level isolation, not a real jail. It does not block
 * network access or limit what files the process can reach on its own. For production
 * use near a bank, each run should later go inside stronger container isolation (for
 * example a locked down container with no network). Process isolation with a hard
 * timeout and a clean temp folder is the safe start, not the finish.
 */
@Component
public class Sandbox {

  /** What a finished (or killed) process left behind. */
  public record Outcome(int exitCode, String stdout, String stderr, boolean timedOut,
      long durationMillis) {

    public boolean ok() {
      return !timedOut && exitCode == 0;
    }
  }

  /** Make a fresh, empty working folder under the system temp area. */
  public Path createWorkDir(String prefix) throws IOException {
    return Files.createTempDirectory(prefix + "-");
  }

  /**
   * Run a command in the given folder, feeding it the given text on standard input, and
   * wait up to the timeout. Extra environment entries are added on top of the inherited
   * environment (used to point cobc at its compiler and config).
   */
  public Outcome run(List<String> command, Path workDir, String stdin,
      Map<String, String> extraEnv, Duration timeout) throws IOException, InterruptedException {

    Path inFile = workDir.resolve("__stdin");
    Path outFile = workDir.resolve("__stdout");
    Path errFile = workDir.resolve("__stderr");
    Files.writeString(inFile, stdin == null ? "" : stdin, StandardCharsets.UTF_8);

    ProcessBuilder builder = new ProcessBuilder(command);
    builder.directory(workDir.toFile());
    builder.redirectInput(inFile.toFile());
    builder.redirectOutput(outFile.toFile());
    builder.redirectError(errFile.toFile());
    if (extraEnv != null && !extraEnv.isEmpty()) {
      // On Windows this map is case insensitive, so setting "PATH" replaces "Path".
      builder.environment().putAll(extraEnv);
    }

    long start = System.currentTimeMillis();
    Process process = builder.start();
    boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
    boolean timedOut = false;
    if (!finished) {
      timedOut = true;
      process.destroyForcibly();
      // Give it a moment to actually die so we can read whatever it wrote.
      process.waitFor(5, TimeUnit.SECONDS);
    }
    long durationMillis = System.currentTimeMillis() - start;

    int exitCode = timedOut ? -1 : process.exitValue();
    String out = readIfPresent(outFile);
    String err = readIfPresent(errFile);
    return new Outcome(exitCode, out, err, timedOut, durationMillis);
  }

  /** Delete the working folder and everything in it. Safe to call more than once. */
  public void cleanUp(Path workDir) {
    if (workDir == null) {
      return;
    }
    try (var paths = Files.walk(workDir)) {
      paths.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {
          // A leftover temp file is not worth failing a verification run over.
        }
      });
    } catch (IOException ignored) {
      // The folder is in the system temp area and will be cleaned up by the OS anyway.
    }
  }

  private static String readIfPresent(Path file) throws IOException {
    if (!Files.exists(file)) {
      return "";
    }
    return Files.readString(file, StandardCharsets.UTF_8);
  }
}
