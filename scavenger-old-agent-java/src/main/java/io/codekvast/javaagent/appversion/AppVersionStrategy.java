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
package io.codekvast.javaagent.appversion;

import java.io.File;
import java.util.Collection;

/**
 * Strategy for how to obtain the version of an application.
 *
 * @author olle.hallin@crisp.se
 */
public interface AppVersionStrategy {

    String UNKNOWN_VERSION = "<unknown>";

    /**
     * Can this strategy handle these args?
     *
     * @param args The white-space separated value from AgentConfig.appVersion
     * @return true if-and-only-if the strategy recognizes the args.
     */
    boolean canHandle(String[] args);

    /**
     * Use args for resolving the app version
     *
     * @param codeBases The locations of the code base.
     * @param args      The value of AgentConfig.appVersion
     * @return The resolved application version.
     */
    String resolveAppVersion(Collection<File> codeBases, String[] args);
}
