package com.navercorp.scavenger.util;

import org.apache.commons.codec.digest.MurmurHash3;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Callable;

public class HashGenerator {

    public static class DefaultHash {
        public static String from(String signature) {
            return Murmur.from(signature);
        }
    }

    private static class Murmur {
        private static String from(String signature) {
            long[] x64hash = MurmurHash3.hash128x64(signature.getBytes(StandardCharsets.UTF_8));
            return Long.toHexString(x64hash[0]) + Long.toHexString(x64hash[1]);
        }
    }

    private static class Sha256 {
        private static String from(String signature) {
            MessageDigest md = callWithCheckedExceptionWrapping(() -> MessageDigest.getInstance("SHA-256"));
            md.update(signature.getBytes(StandardCharsets.UTF_8));
            return String.format("%x", new BigInteger(1, md.digest()));
        }
    }

    private static class Md5 {
        private static String from(String signature) {
            MessageDigest md = callWithCheckedExceptionWrapping(() -> MessageDigest.getInstance("MD5"));
            md.update(signature.getBytes(StandardCharsets.UTF_8));
            return String.format("%x", new BigInteger(1, md.digest()));
        }
    }

    /**
     * Utility method to call a {@link Callable} and rethrow any exceptions as unchecked.
     */
    private static <T> T callWithCheckedExceptionWrapping(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
