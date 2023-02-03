package com.navercorp.scavenger.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {
    public static class Sha256 {
        public static String from(String signature) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(signature.getBytes(StandardCharsets.UTF_8));
                return String.format("%x", new BigInteger(1, md.digest()));
            } catch (NoSuchAlgorithmException ignore) {
                // ignore
                return null;
            }
        }
    }

    public static class Md5 {
        public static String from(String signature) {
            try {
                if (signature == null) {
                    return null;
                }
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(signature.getBytes(StandardCharsets.UTF_8));
                return String.format("%x", new BigInteger(1, md.digest()));
            } catch (NoSuchAlgorithmException ignore) {
                // ignore
                return null;
            }
        }
    }
}
