package com.navercorp.scavenger.javaagent.model;

import org.objectweb.asm.Opcodes;

public enum Visibility {
    PUBLIC("public"),
    PROTECTED("protected"),
    PACKAGE_PRIVATE("package-private"),
    PRIVATE("private");

    private final String string;

    Visibility(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

    public static Visibility from(String string) {
        if (PUBLIC.string.equals(string)) {
            return PUBLIC;
        }

        if (PROTECTED.string.equals(string)) {
            return PROTECTED;
        }

        if (PRIVATE.string.equals(string)) {
            return PRIVATE;
        }

        return PACKAGE_PRIVATE;
    }

    public static Visibility from(int access) {
        if ((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
            return PUBLIC;
        }

        if ((access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
            return PROTECTED;
        }

        if ((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) {
            return PRIVATE;
        }

        return PACKAGE_PRIVATE;
    }
}
