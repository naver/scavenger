/*
 * Copyright (c) 2015-2022 Hallin Information Technology AB
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
package io.codekvast.javaagent.model.v4;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A response object for {@link GetConfigRequest4}.
 * modified by NAVER: bump up version to 4
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@SuppressWarnings("ClassWithTooManyFields")
@Value
@Jacksonized
@Builder(toBuilder = true)
public class GetConfigResponse4 {
    private static final long serialVersionUID = 4L;

    /**
     * What customerId should I use when publishing data?
     */
    @NonNull
    Long customerId;

    /**
     * How often shall the server be polled for dynamic config?
     */
    @NonNull
    Integer configPollIntervalSeconds;

    /**
     * How fast shall a failed config poll be retried?
     */
    @NonNull
    Integer configPollRetryIntervalSeconds;

    /**
     * The name of the code base publisher to use.
     *
     * <p>Each implementation defines it's own name.
     */
    @NonNull
    String codeBasePublisherName;

    /**
     * The configuration of the CodeBasePublisher to use, coded as semicolon-separated list of
     * key=value pairs. Neither keys nor values may contains semicolons, space or tab characters.
     *
     * <p>It is up to the specified CodeBasePublisher to parse the config.
     */
    @NonNull
    String codeBasePublisherConfig;

    /**
     * How often shall the codebase be re-scanned for changes?
     */
    @NonNull
    Integer codeBasePublisherCheckIntervalSeconds;

    /**
     * How often shall a failed codebase publishing be retried?
     */
    @NonNull
    Integer codeBasePublisherRetryIntervalSeconds;

    /**
     * The name of the invocation data publisher to use.
     *
     * <p>Each implementation defines it's own name.
     */
    @NonNull
    String invocationDataPublisherName;

    /**
     * The configuration of the InvocationDataPublisher to use, coded as semicolon-separated list of
     * key=value pairs. Neither keys nor values may contains semicolons, space or tab characters.
     *
     * <p>It is up to the specified InvocationDataPublisher to parse the config.
     */
    @NonNull
    String invocationDataPublisherConfig;

    /**
     * How often shall the invocation data be published?
     */
    @NonNull
    Integer invocationDataPublisherIntervalSeconds;

    /**
     * How often shall a failed invocation data publishing be retried?
     */
    @NonNull
    Integer invocationDataPublisherRetryIntervalSeconds;

    public static GetConfigResponse4 sample() {
        return builder()
            .customerId(1L)
            .configPollIntervalSeconds(60)
            .configPollRetryIntervalSeconds(60)
            .codeBasePublisherConfig("enabled=true")
            .codeBasePublisherName("no-op")
            .codeBasePublisherCheckIntervalSeconds(60)
            .codeBasePublisherRetryIntervalSeconds(60)
            .invocationDataPublisherConfig("enabled=true")
            .invocationDataPublisherName("no-op")
            .invocationDataPublisherIntervalSeconds(60)
            .invocationDataPublisherRetryIntervalSeconds(60)
            .build();
    }
}
