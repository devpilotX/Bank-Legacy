package com.corewise.modernization.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VerificationComparatorTest {

    @Test
    void passesWhenOutputsMatch() {
        VerificationComparator.Result result =
            VerificationComparator.evaluate("BALANCE: 100.00", "BALANCE: 100.00");
        assertThat(result.passed()).isTrue();
    }

    @Test
    void ignoresTrailingSpacesAndOuterBlankLines() {
        VerificationComparator.Result result =
            VerificationComparator.evaluate("line one\nline two", "\nline one   \nline two\n");
        assertThat(result.passed()).isTrue();
    }

    @Test
    void failsWhenAValueDiffers() {
        VerificationComparator.Result result =
            VerificationComparator.evaluate("BALANCE: 100.00", "BALANCE: 100.01");
        assertThat(result.passed()).isFalse();
        assertThat(result.detail()).contains("Line 1");
    }

    @Test
    void failsWhenLineCountDiffers() {
        VerificationComparator.Result result =
            VerificationComparator.evaluate("one\ntwo", "one");
        assertThat(result.passed()).isFalse();
    }
}
