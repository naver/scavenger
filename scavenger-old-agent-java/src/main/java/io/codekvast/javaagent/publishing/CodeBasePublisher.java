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
package io.codekvast.javaagent.publishing;

import io.codekvast.javaagent.codebase.CodeBase;
import io.codekvast.javaagent.codebase.CodeBaseFingerprint;

/**
 * Strategy for publishing a {@link CodeBase}
 */
public interface CodeBasePublisher extends Publisher {

    /**
     * Publishes a codebase.
     *
     * @throws CodekvastPublishingException when no contact with the consumer. Try again.
     */
    void publishCodeBase() throws CodekvastPublishingException;

    /**
     * Retrieve the latest CodeBaseFingerprint
     *
     * @return The latest CodeBaseFingerprint
     */
    CodeBaseFingerprint getCodeBaseFingerprint();

    /**
     * How many times has the code base been checked for differences?
     *
     * @return The number of times the code base has been scanned for changes.
     */
    int getCodeBaseCheckCount();
}
