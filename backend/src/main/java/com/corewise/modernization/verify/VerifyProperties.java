package com.corewise.modernization.verify;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the verification engine, all from config so nothing about this machine
 * is baked into the code.
 *
 * <p>gnucobolHome points at the GnuCOBOL install. When it is set, the engine puts its
 * bin folder on PATH and sets the COB_* variables for the child process, so cobc can
 * find its compiler and config. When it is blank, the engine just trusts cobc on PATH.
 */
@ConfigurationProperties(prefix = "app.verify")
public record VerifyProperties(
    Boolean enabled,
    String gnucobolHome,
    String cobcCommand,
    String javacCommand,
    String javaCommand,
    Duration compileTimeout,
    Duration runTimeout,
    String workDir,
    Boolean trimTrailingSpace,
    Double numericTolerance) {

  public VerifyProperties {
    if (enabled == null) {
      enabled = Boolean.TRUE;
    }
    if (cobcCommand == null || cobcCommand.isBlank()) {
      cobcCommand = "cobc";
    }
    if (javacCommand == null || javacCommand.isBlank()) {
      javacCommand = "javac";
    }
    if (javaCommand == null || javaCommand.isBlank()) {
      javaCommand = "java";
    }
    // Compiling can be slow the first time; running a small program should be quick.
    if (compileTimeout == null) {
      compileTimeout = Duration.ofSeconds(60);
    }
    if (runTimeout == null) {
      runTimeout = Duration.ofSeconds(10);
    }
    // The default comparison settings. A run can override these.
    if (trimTrailingSpace == null) {
      trimTrailingSpace = Boolean.TRUE;
    }
    if (numericTolerance == null) {
      numericTolerance = 0.0;
    }
  }
}
