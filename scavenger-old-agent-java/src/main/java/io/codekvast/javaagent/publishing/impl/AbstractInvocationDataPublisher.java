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
package io.codekvast.javaagent.publishing.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import io.codekvast.javaagent.codebase.CodeBaseFingerprint;
import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.publishing.CodekvastPublishingException;
import io.codekvast.javaagent.publishing.InvocationDataPublisher;
import io.codekvast.javaagent.util.SignatureUtils;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 * @author olle.hallin@crisp.se
 */
@Log
@Getter
public abstract class AbstractInvocationDataPublisher extends AbstractPublisher
    implements InvocationDataPublisher {

    private CodeBaseFingerprint codeBaseFingerprint;

    AbstractInvocationDataPublisher(Logger log, AgentConfig config) {
        super(log, config);
    }

    @Override
    public void setCodeBaseFingerprint(CodeBaseFingerprint fingerprint) {
        codeBaseFingerprint = fingerprint;
    }

    @Override
    public void publishInvocationData(long recordingIntervalStartedAtMillis, Set<String> invocations)
        throws CodekvastPublishingException {
        if (isEnabled() && getCodeBaseFingerprint() != null) {
            incrementSequenceNumber();

            log.fine("Publishing invocation data #" + this.getSequenceNumber());

            doPublishInvocationData(recordingIntervalStartedAtMillis, normalizeSignatures(invocations));
        }
    }

    private Set<String> normalizeSignatures(Set<String> invocations) {
        Set<String> result = new HashSet<>();
        for (String s : invocations) {
            String normalizedSignature = SignatureUtils.normalizeSignature(s);
            if (normalizedSignature != null) {
                result.add(SignatureUtils.stripModifiers(normalizedSignature));
            }
        }
        return result;
    }

    abstract void doPublishInvocationData(
        long recordingIntervalStartedAtMillis, Set<String> invocations)
        throws CodekvastPublishingException;
}
