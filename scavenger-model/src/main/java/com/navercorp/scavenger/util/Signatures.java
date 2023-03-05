package com.navercorp.scavenger.util;

import java.util.regex.Pattern;

public final class Signatures {

    private static final Pattern SYNTHETIC_SIGNATURE_PATTERN = Pattern.compile(".*\\$\\$(Enhancer|FastClass)BySpringCGLIB\\$\\$.*");

    private Signatures() {
        // utility class
    }

    public static boolean containsSyntheticPattern(String signature) {
        return SYNTHETIC_SIGNATURE_PATTERN.matcher(signature).matches();
    }
}
