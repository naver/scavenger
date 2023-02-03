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
import io.codekvast.javaagent.publishing.InvocationDataPublisher;
import io.codekvast.javaagent.publishing.InvocationDataPublisherFactory;
import lombok.extern.java.Log;

/**
 * A factory for InvocationDataPublisher instances.
 *
 * @author olle.hallin@crisp.se
 */
@Log
public class InvocationDataPublisherFactoryImpl implements InvocationDataPublisherFactory {
    /**
     * Creates an instance of the InvocationDataPublisher strategy.
     *
     * @param name   The name of the strategy to create.
     * @param config Is passed to the created strategy.
     * @return A configured implementation of InvocationDataPublisher
     */
    @Override
    public InvocationDataPublisher create(String name, AgentConfig config) {
        if (name.equals(NoOpInvocationDataPublisherImpl.NAME)) {
            return new NoOpInvocationDataPublisherImpl(config);
        }

        if (name.equals(HttpInvocationDataPublisherImpl.NAME)) {
            return new HttpInvocationDataPublisherImpl(config);
        }

        log.warning(
            String.format(
                "Unrecognized invocation data publisher name: '%s', will use %s",
                name, NoOpInvocationDataPublisherImpl.NAME));
        return new NoOpInvocationDataPublisherImpl(config);
    }
}
