package com.navercorp.scavenger.javaagent.collecting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import lombok.Getter;

import com.navercorp.scavenger.util.HashGenerator;
import com.navercorp.scavenger.util.Signatures;

public class MethodRegistry {
    public static final String SYNTHETIC_SIGNATURE_HASH = "";

    @Getter
    private final Map<String, String> byteBuddySignatureToHash = new ConcurrentHashMap<>();

    public String getHash(String byteBuddySignature, boolean legacyCompatibilityMode) {
        String hash = this.byteBuddySignatureToHash.get(byteBuddySignature);

        if (hash == null) {
            if (Signatures.containsSyntheticPattern(byteBuddySignature)) {
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

    private String extractSignature(String byteBuddySignature) {
        int begin = 0;
        int end = byteBuddySignature.length();
        boolean isParenthesisFound = false;

        for (int pos = 0; pos < end; pos++) {
            if (byteBuddySignature.charAt(pos) == ' ') {
                if (!isParenthesisFound) {
                    begin = pos + 1;
                } else {
                    end = pos;
                }
            } else if (byteBuddySignature.charAt(pos) == '(') {
                isParenthesisFound = true;
            }
        }

        return byteBuddySignature.substring(begin, end);
    }
}
