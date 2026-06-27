package com.corewise.modernization.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CobolReferenceParserTest {

    @Test
    void findsCallsAndCopiesAndSkipsComments() {
        String source = String.join("\n",
            "       IDENTIFICATION DIVISION.",
            "       PROGRAM-ID. MAIN.",
            "       PROCEDURE DIVISION.",
            "           COPY CUSTREC.",
            "           CALL 'SUBPROG' USING WS-A.",
            "      * CALL 'IGNORED' this line is a comment",
            "           CALL \"PAYCALC\".");

        CobolReferenceParser.References refs = CobolReferenceParser.parse(source);

        assertThat(refs.calls()).containsExactlyInAnyOrder("SUBPROG", "PAYCALC");
        assertThat(refs.copies()).containsExactly("CUSTREC");
    }

    @Test
    void handlesEmptyContent() {
        CobolReferenceParser.References refs = CobolReferenceParser.parse("");
        assertThat(refs.calls()).isEmpty();
        assertThat(refs.copies()).isEmpty();
    }
}
