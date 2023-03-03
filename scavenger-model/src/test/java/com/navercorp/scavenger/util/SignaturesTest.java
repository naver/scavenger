package com.navercorp.scavenger.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SignaturesTest {

    @Test
    void returnsTrueIfSignatureContainsEnhancerBySpringCGLIB() {
        // given
        String signature = "TestClass$$EnhancerBySpringCGLIB$$hash.testMethod()";

        // when
        boolean actual = Signatures.containsSyntheticPattern(signature);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void returnsTrueIfSignatureContainsFastClassBySpringCGLIB() {
        // given
        String signature = "TestClass$$FastClassBySpringCGLIB$$hash.testMethod()";

        // when
        boolean actual = Signatures.containsSyntheticPattern(signature);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void returnsFalseIfDoesNotContainsPattern() {
        // given
        String signature = "TestClass$hash.testMethod()";

        // when
        boolean actual = Signatures.containsSyntheticPattern(signature);

        // then
        assertThat(actual).isFalse();
    }
}
