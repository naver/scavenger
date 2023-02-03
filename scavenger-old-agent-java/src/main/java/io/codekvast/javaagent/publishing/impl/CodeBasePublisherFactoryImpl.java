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
import io.codekvast.javaagent.publishing.CodeBasePublisher;
import io.codekvast.javaagent.publishing.CodeBasePublisherFactory;
import lombok.extern.java.Log;

/**
 * Factory for CodeBasePublisher implementations.
 *
 * @author olle.hallin@crisp.se
 */
@Log
public class CodeBasePublisherFactoryImpl implements CodeBasePublisherFactory {

    /**
     * Creates an instance of the CodeBasePublisher strategy.
     *
     * @param name   The name of the strategy to create.
     * @param config Is passed to the created strategy.
     * @return A configured implementation of CodeBasePublisher
     */
    @Override
    public CodeBasePublisher create(String name, AgentConfig config) {
        if (name.equals(NoOpCodeBasePublisherImpl.NAME)) {
            return new NoOpCodeBasePublisherImpl(config);
        }

        if (name.equals(HttpCodeBasePublisherImpl.NAME)) {
            return new HttpCodeBasePublisherImpl(config);
        }

        log.warning(
            String.format(
                "Unrecognized code base publisher name: '%s', will use %s",
                name, NoOpCodeBasePublisherImpl.NAME));
        return new NoOpCodeBasePublisherImpl(config);
    }
}
