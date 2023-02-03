package com.navercorp.scavenger.javaagent.model;

import static java.nio.charset.StandardCharsets.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.Value;

import com.navercorp.scavenger.model.CodeBasePublication;

@Value
public class CodeBase {
    List<Method> methods;
    String codeBaseFingerprint;

    public CodeBase(Config config, List<Method> methods) {
        this.methods = methods;
        this.codeBaseFingerprint = calculateFingerprint(config, methods);
    }

    public CodeBase(List<Method> methods, String codeBaseFingerprint) {
        this.methods = methods;
        this.codeBaseFingerprint = codeBaseFingerprint;
    }

    public CodeBasePublication toPublication(Config config) {
        return CodeBasePublication.newBuilder()
            .setCommonData(
                config.buildCommonPublicationData().toBuilder()
                    .setCodeBaseFingerprint(codeBaseFingerprint)
                    .build()
            )
            .addAllEntry
                (
                    methods.stream()
                        .map(Method::toCodeBaseEntry)
                        .collect(Collectors.toList())
                )
            .build();
    }

    static byte[] longToBytes(long l) {
        long value = l;
        byte[] result = new byte[Long.SIZE / Byte.SIZE];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte)(value & 0xFF);
            value >>= Byte.SIZE;
        }
        return result;
    }

    @SneakyThrows
    private static String calculateFingerprint(Config config, List<Method> methods) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(config.getCodeBase().toString().getBytes(UTF_8));
        md.update(config.getPackages().toString().getBytes(UTF_8));
        md.update(config.getExcludePackages().toString().getBytes(UTF_8));
        md.update(config.getAdditionalPackages().toString().getBytes(UTF_8));
        md.update(config.getAnnotations().toString().getBytes(UTF_8));
        md.update(config.getMethodVisibility().toString().getBytes(UTF_8));
        md.update((byte)(config.isExcludeConstructors() ? 1 : 0));
        md.update((byte)(config.isExcludeGetterSetter() ? 1 : 0));
        md.update((byte)(config.isLegacyCompatibilityMode() ? 1 : 0));

        md.update(longToBytes(methods.size()));
        for (Method each : methods) {
            md.update(each.getSignature().getBytes(UTF_8));
        }

        return String.format("%x", new BigInteger(1, md.digest()));
    }
}
