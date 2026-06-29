package com.corewise.modernization.verify;

/**
 * What happened when we compiled and ran one side, either the original COBOL or the new
 * Java. The engine reads this to decide the overall result of a case.
 *
 * @param compiled did it compile
 * @param ran did it run and finish on its own with a success exit code
 * @param timedOut did we have to kill it for running too long
 * @param output what it wrote to standard output (the result we compare)
 * @param detail a plain message: the compile error, or the run error, when something
 *     went wrong; otherwise empty
 */
public record ProgramResult(
    boolean compiled,
    boolean ran,
    boolean timedOut,
    String output,
    String detail) {

  public static ProgramResult compileFailed(String detail) {
    return new ProgramResult(false, false, false, "", detail);
  }

  public static ProgramResult timedOut(String detail) {
    return new ProgramResult(true, false, true, "", detail);
  }

  public static ProgramResult runFailed(String detail) {
    return new ProgramResult(true, false, false, "", detail);
  }

  public static ProgramResult ranOk(String output) {
    return new ProgramResult(true, true, false, output, "");
  }
}
