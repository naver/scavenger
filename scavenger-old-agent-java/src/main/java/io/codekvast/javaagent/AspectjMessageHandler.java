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
package io.codekvast.javaagent;

import static org.aspectj.bridge.IMessage.Kind;
import static org.aspectj.bridge.IMessage.WEAVEINFO;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;

import lombok.extern.java.Log;

/**
 * A bridge from AspectJ's IMessageHandler to java.util.logging (JUL)
 */
@SuppressWarnings("MethodReturnAlwaysConstant")
@Log(topic = "io.codekvast.aspectjweaver")
public class AspectjMessageHandler implements IMessageHandler {
    public static final String LOGGER_NAME = "io.codekvast.aspectjweaver";

    @Override
    public boolean handleMessage(IMessage message) {
        if (message.isDebug()) {

            String m = message.getMessage();
            if (!m.contains("not weaving") || !m.contains("codekvast")) {
                log.fine(m);
            }
            return true;
        }
        if (message.isInfo()) {
            log.info(message.getMessage());
            return true;
        }
        if (message.isWarning()) {
            return true;
        }
        if (message.isError()) {
            log.severe(message.getMessage());
            return true;
        }
        if (message.getKind() == WEAVEINFO) {
            log.info(message.getMessage());
            return true;
        }
        return false;
    }

    @Override
    public boolean isIgnoring(Kind kind) {
        return false;
    }

    @Override
    public void dontIgnore(Kind kind) {
        // No-op
    }

    @Override
    public void ignore(Kind kind) {
        // No-op
    }
}
