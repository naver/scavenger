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

import java.io.File;
import java.util.Set;

import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.model.v4.InvocationDataPublication4;
import io.codekvast.javaagent.publishing.CodekvastPublishingException;
import io.codekvast.javaagent.util.FileUtils;
import io.codekvast.javaagent.util.LogUtil;
import lombok.extern.java.Log;

/**
 * A HTTP implementation of InvocationDataPublisher.
 *
 * <p>It uses the FileSystemInvocationDataPublisherImpl for creating a file, which then is POSTed to
 * the server.
 * modified by NAVER: remove several parameters, send empty invocation
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@Log
public class HttpInvocationDataPublisherImpl extends AbstractInvocationDataPublisher {

    static final String NAME = "http";

    private long lastSentMillis = System.currentTimeMillis();
    private static final long MINIMUM_INVOCATION_SENDING_DURATION_MILLIS = 1000 * 60 * 10; // 10 min

    HttpInvocationDataPublisherImpl(AgentConfig config) {
        super(log, config);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    void doPublishInvocationData(long recordingIntervalStartedAtMillis, Set<String> invocations)
        throws CodekvastPublishingException {

        if (invocations.isEmpty() && System.currentTimeMillis() - lastSentMillis < MINIMUM_INVOCATION_SENDING_DURATION_MILLIS) {
            log.fine("not sending invocation because no invocation is made.");
            return;
        } else {
            log.fine("sending invocation.");
        }

        String url = getConfig().getInvocationDataUploadEndpoint();
        File file = null;
        try {

            InvocationDataPublication4 publication =
                createPublication(getCustomerId(), recordingIntervalStartedAtMillis, invocations);
            file =
                FileUtils.serializeToFile(
                    publication, getConfig().getFilenamePrefix("invocations-"), ".ser");

            doPost(file, url);

            lastSentMillis = System.currentTimeMillis();
            log.fine(
                String.format(
                    "Scavenger uploaded %d invocations (%s) to %s",
                    publication.getInvocations().size(),
                    LogUtil.humanReadableByteCount(file.length()),
                    url));

        } catch (Exception e) {
            throw new CodekvastPublishingException("Cannot upload invocation data to " + url, e);
        } finally {
            FileUtils.safeDelete(file);
        }
    }

    private InvocationDataPublication4 createPublication(
        long customerId, long recordingIntervalStartedAtMillis, Set<String> invocations) {

        return InvocationDataPublication4.builder()
            .commonData(
                getConfig().commonPublicationData().toBuilder()
                    .codeBaseFingerprint(getCodeBaseFingerprint().toString())
                    .customerId(customerId)
                    .sequenceNumber(this.getSequenceNumber())
                    .build())
            .recordingIntervalStartedAtMillis(recordingIntervalStartedAtMillis)
            .invocations(invocations)
            .build();
    }
}
