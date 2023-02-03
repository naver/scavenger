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

import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.publishing.Publisher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static io.codekvast.javaagent.model.Endpoints.Agent.PARAM_LICENSE_KEY;
import static io.codekvast.javaagent.model.Endpoints.Agent.PARAM_PUBLICATION_FILE;

/**
 * Abstract base class for publishers.
 * modified by NAVER: remove several parameters
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@Log
@Getter
public abstract class AbstractPublisher implements Publisher {
    private static final MediaType APPLICATION_OCTET_STREAM =
        MediaType.parse("application/octet-stream");
    protected final Logger logger;
    private final AgentConfig config;
    @Setter
    private boolean enabled;

    private long customerId = -1L;

    private int sequenceNumber;

    AbstractPublisher(Logger logger, AgentConfig config) {
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void configure(long customerId, String keyValuePairs) {
        if (customerId != this.customerId) {
            this.customerId = customerId;
            log.fine("Using customerId " + customerId);
        }

        String[] pairs = keyValuePairs.split(";");

        for (String pair : pairs) {
            pair = pair.trim();
            if (!pair.isEmpty()) {
                log.finest("Analyzing " + pair);
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    setValue(parts[0].trim(), parts[1].trim());
                } else {
                    log.warning("Illegal key-value pair: " + pair);
                }
            }
        }
    }

    private void setValue(String key, String value) {
        if (key.equals("enabled")) {
            boolean newValue = Boolean.parseBoolean(value);
            boolean oldValue = this.enabled;
            if (oldValue != newValue) {
                log.fine(String.format("Setting %s=%s, was=%s", key, newValue, this.enabled));
                this.enabled = newValue;
            }
        } else {
            boolean recognized = doSetValue(key, value);
            if (recognized) {
                log.fine(String.format("Setting %s=%s", key, value));
            } else {
                log.warning(String.format("Unrecognized key-value pair: %s=%s", key, value));
            }
        }
    }

    void incrementSequenceNumber() {
        sequenceNumber += 1;
    }

    /**
     * Override in concrete subclasses to handle private configuration settings.
     *
     * @param key   The name of the parameter.
     * @param value The value of the parameter.
     * @return true iff the key was recognized.
     */
    @SuppressWarnings("SameReturnValue")
    boolean doSetValue(String key, String value) {
        return false;
    }

    void doPost(File file, String url) throws IOException {
        RequestBody requestBody =
            new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(PARAM_LICENSE_KEY, getConfig().getLicenseKey())
                .addFormDataPart(
                    PARAM_PUBLICATION_FILE,
                    file.getName(),
                    RequestBody.create(APPLICATION_OCTET_STREAM, file))
                .build();

        Request request = new Request.Builder().url(url).post(requestBody).build();
        try (Response response = executeRequest(request)) {
            if (!response.isSuccessful()) {
                throw new IOException(response.body().string());
            }
        }
    }

    // Make it simple to subclass and override in tests...
    Response executeRequest(Request request) throws IOException {
        return getConfig().getHttpClient().newCall(request).execute();
    }
}
