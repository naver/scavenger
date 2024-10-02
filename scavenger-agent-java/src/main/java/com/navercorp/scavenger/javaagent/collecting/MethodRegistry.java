package com.navercorp.scavenger.javaagent.collecting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.extern.java.Log;

import com.navercorp.scavenger.util.HashGenerator;

@Log
public class MethodRegistry {
    private static final String SYNTHETIC_SIGNATURE_HASH = "";
    private static final Pattern SYNTHETIC_SIGNATURE_PATTERN = Pattern.compile(".*\\$\\$(Enhancer|FastClass)BySpringCGLIB\\$\\$.*");

    @Getter
    private final Map<String, String> byteBuddySignatureToHash = new ConcurrentHashMap<>();
    private final boolean isLegacyCompatibilityMode;

    public MethodRegistry(boolean isLegacyCompatibilityMode) {
        this.isLegacyCompatibilityMode = isLegacyCompatibilityMode;
    }

    public String getHash(String byteBuddySignature) {
        return byteBuddySignatureToHash.computeIfAbsent(byteBuddySignature, this::generateHash);
    }

    @SuppressWarnings("StringEquality")
    public static boolean isSyntheticSignatureHash(String hash) {
        return SYNTHETIC_SIGNATURE_HASH == hash;
    }

    private String generateHash(String byteBuddySignature) {
        if (SYNTHETIC_SIGNATURE_PATTERN.matcher(byteBuddySignature).matches()) {
            return SYNTHETIC_SIGNATURE_HASH;
        }
        String signature = extractSignature(byteBuddySignature);
        if (isLegacyCompatibilityMode) {
            signature = signature.replace('$', '.').replace(",", ", ");
        }
        return HashGenerator.Md5.from(signature);
    }

    static String extractSignature(String byteBuddySignature) {
        int begin = 0;
        int end = byteBuddySignature.length();
        boolean isParenthesisFound = false;
        for (int pos = end - 1; pos >= 0; pos--) {
            if (isParenthesisFound) {
                if (byteBuddySignature.charAt(pos) == ' ') {
                    begin = pos + 1;
                    break;
                }
            } else if (byteBuddySignature.charAt(pos) == '(') {
                isParenthesisFound = true;
            }
        }
        return byteBuddySignature.substring(begin, end);
    }
}
