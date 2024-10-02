package com.navercorp.scavenger.javaagent.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.navercorp.scavenger.util.HashGenerator.DefaultHash;

class MethodTest {
    @Test
    void testMethodCreation() {
        Method method = new Method(
            "name", Visibility.PRIVATE, "static", false, false, "packageName.declaringType", "int a,int wow", "name(int a, int wow)"
        );
        assertThat(method.toCodeBaseEntry()).satisfies(it -> {
            assertThat(it.getDeclaringType()).isEqualTo("packageName.declaringType");
            assertThat(it.getMethodName()).isEqualTo("name");
            assertThat(it.getVisibility()).isEqualTo("private");
            assertThat(it.getModifiers()).isEqualTo("static");
            assertThat(it.getPackageName()).isEqualTo("packageName");
            assertThat(it.getSignature()).isEqualTo("name(int a, int wow)");
            assertThat(it.getSignatureHash()).isEqualTo(DefaultHash.from("name(int a, int wow)"));
        });

        Method method2 = new Method(null, Visibility.PRIVATE, null, false, false, "", "int a,int wow", null);
        assertThat(method2.toCodeBaseEntry()).satisfies(it -> {
            assertThat(it.getDeclaringType()).isEqualTo("");
            assertThat(it.getMethodName()).isEqualTo("");
            assertThat(it.getVisibility()).isEqualTo("private");
            assertThat(it.getModifiers()).isEqualTo("");
            assertThat(it.getPackageName()).isEqualTo("");
            assertThat(it.getSignature()).isEqualTo("");
            assertThat(it.getSignatureHash()).isEqualTo(DefaultHash.from(""));
        });
    }

}
