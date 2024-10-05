package com.navercorp.scavenger.javaagent.collecting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import lombok.Getter;

import com.navercorp.scavenger.util.HashGenerator;

public class MethodRegistry {
    public static final String SYNTHETIC_SIGNATURE_HASH = "";

    @Getter
    private final Map<String, String> byteBuddySignatureToHash = new ConcurrentHashMap<>();

    private static final Pattern SYNTHETIC_SIGNATURE_PATTERN = Pattern.compile(".*\\$\\$(Enhancer|FastClass)BySpringCGLIB\\$\\$.*");

    public String getHash(String byteBuddySignature, boolean legacyCompatibilityMode) {
        String hash = this.byteBuddySignatureToHash.get(byteBuddySignature);

        if (hash == null) {
            if (SYNTHETIC_SIGNATURE_PATTERN.matcher(byteBuddySignature).matches()) {
                hash = SYNTHETIC_SIGNATURE_HASH;
            } else {
                String signature = extractSignature(byteBuddySignature);
                if (legacyCompatibilityMode) {
                    signature = signature.replace('$', '.');
                    signature = signature.replace(",", ", ");
                }
                hash = HashGenerator.Md5.from(signature);
            }
            this.byteBuddySignatureToHash.put(byteBuddySignature, hash);
        }

        return hash;
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
