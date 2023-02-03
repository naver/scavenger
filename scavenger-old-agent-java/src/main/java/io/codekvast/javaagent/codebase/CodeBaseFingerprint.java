/*
 * Copyright (c) 2015-2021 Hallin Information Technology AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.codekvast.javaagent.codebase;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Set;
import java.util.TreeSet;

import io.codekvast.javaagent.config.AgentConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.java.Log;
import lombok.val;

/**
 * An immutable fingerprint of a code base. Used for comparing different code bases for equality.
 * modified by NAVER: add annotation/constructor filtering option
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@Value
@RequiredArgsConstructor
@Log
public class CodeBaseFingerprint {
    int numClassFiles;
    int numJarFiles;

    @NonNull
    String sha256;

    public static Builder builder(AgentConfig config) {
        return new Builder(config);
    }

    int getNumFiles() {
        return numClassFiles + numJarFiles;
    }

    /**
     * Builder for incrementally building a CodeBaseFingerprint
     */
    @RequiredArgsConstructor
    public static class Builder {
        private final AgentConfig config;

        private final Set<File> files = new TreeSet<>();

        Builder record(File file) {
            if (files.add(file)) {
                log.finest("Recorded " + file);
            } else {
                log.fine("Ignored duplicate file " + file);
            }
            return this;
        }

        byte[] longToBytes(long l) {
            long value = l;
            byte[] result = new byte[Long.SIZE / Byte.SIZE];
            for (int i = 0; i < result.length; i++) {
                result[i] = (byte)(value & 0xFF);
                value >>= Byte.SIZE;
            }
            return result;
        }

        @SneakyThrows
        public CodeBaseFingerprint build() {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(config.getSeparatedPackages().toString().getBytes(UTF_8));
            md.update(config.getSeparatedExcludePackages().toString().getBytes(UTF_8));
            md.update(config.getMethodAnalyzer().toString().getBytes(UTF_8));
            md.update(config.getSeparatedAnnotations().toString().getBytes(UTF_8));
            md.update(config.getSeparatedAdditionalPackages().toString().getBytes(UTF_8));
            md.update((byte)(config.getExcludeConstructors() ? 1 : 0));
            md.update(longToBytes(files.size()));
            for (File eachCodeBase : config.getCodeBaseFiles()) {
                md.update(eachCodeBase.getName().getBytes(UTF_8));
            }
            int numClassFiles = 0;
            int numJarFiles = 0;

            for (File file : files) {
                md.update(longToBytes(file.length()));
                md.update(longToBytes(file.lastModified()));
                md.update(file.getName().getBytes(UTF_8));

                if (file.getName().endsWith(".class")) {
                    numClassFiles += 1;
                } else {
                    numJarFiles += 1;
                }
            }
            val digest = String.format("%x", new BigInteger(1, md.digest()));
            return new CodeBaseFingerprint(numClassFiles, numJarFiles, digest);
        }
    }
}
