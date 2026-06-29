package com.corewise.modernization.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.corewise.modernization.verify.OutputComparator.ComparisonResult;
import com.corewise.modernization.verify.OutputComparator.DifferenceKind;
import org.junit.jupiter.api.Test;

/** The comparison rules, on their own. These run anywhere, no compiler needed. */
class OutputComparatorTest {

  @Test
  void exactMatchPasses() {
    ComparisonResult result = OutputComparator.compare("INTEREST=000250\n", "INTEREST=000250\n", true, 0);
    assertTrue(result.passed());
    assertEquals(DifferenceKind.NONE, result.kind());
  }

  @Test
  void trailingSpacesAreFormattingOnly() {
    // COBOL often pads with trailing spaces. With trimming on, that is just formatting.
    ComparisonResult result = OutputComparator.compare("HELLO     \n", "HELLO\n", true, 0);
    assertTrue(result.passed());
    assertEquals(DifferenceKind.FORMATTING, result.kind());
  }

  @Test
  void numbersWithinToleranceAreFormattingOnly() {
    ComparisonResult result = OutputComparator.compare("TOTAL=250.00", "TOTAL=250.01", true, 0.05);
    assertTrue(result.passed());
    assertEquals(DifferenceKind.FORMATTING, result.kind());
  }

  @Test
  void aRealDifferenceFailsAndPointsAtIt() {
    ComparisonResult result = OutputComparator.compare("INTEREST=000250", "INTEREST=002500", true, 0);
    assertFalse(result.passed());
    assertEquals(DifferenceKind.BEHAVIOR, result.kind());
    assertTrue(result.diff().contains("000250"));
    assertTrue(result.diff().contains("002500"));
  }
}
