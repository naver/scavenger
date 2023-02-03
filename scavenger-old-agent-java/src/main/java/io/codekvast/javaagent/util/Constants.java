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
package io.codekvast.javaagent.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import lombok.experimental.UtilityClass;

/**
 * @author olle.hallin@crisp.se
 */
@UtilityClass
public class Constants {
    public static final String AGENT_VERSION = getAgentVersion();
    public static final String COMPUTER_ID = ComputerID.compute().toString();
    public static final String HOST_NAME = getHostname();
    public static final String JVM_UUID = UUID.randomUUID().toString();
    public static final long JVM_STARTED_AT_MILLIS = System.currentTimeMillis();

    private static String getAgentVersion() {
        String version = Constants.class.getPackage().getImplementationVersion();
        return version == null ? "dev" : version;
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "-unknown-";
        }
    }
}
