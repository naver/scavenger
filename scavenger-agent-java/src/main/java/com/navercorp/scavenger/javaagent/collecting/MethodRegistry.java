package com.navercorp.scavenger.javaagent.collecting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.extern.java.Log;

import com.navercorp.scavenger.util.HashGenerator;

@Log
public class MethodRegistry {

    @Getter
    private final Map<String, String> byteBuddySignatureToHash = new ConcurrentHashMap<>();
    private final boolean isLegacyCompatibilityMode;

    public MethodRegistry(boolean isLegacyCompatibilityMode) {
        this.isLegacyCompatibilityMode = isLegacyCompatibilityMode;
    }

    public String getHash(String byteBuddySignature) {
        return byteBuddySignatureToHash.computeIfAbsent(byteBuddySignature, this::generateHash);
    }

    private String generateHash(String byteBuddySignature) {
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
