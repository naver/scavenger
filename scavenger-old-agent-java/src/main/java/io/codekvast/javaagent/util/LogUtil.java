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

import static java.lang.String.format;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.experimental.UtilityClass;

/**
 * Utility class for consistent logging.
 */
@UtilityClass
public class LogUtil {

    public static void logException(Logger log, String msg, Exception e) {
        Throwable rootCause = getRootCause(e);
        if (log.isLoggable(Level.FINER)
            && !(rootCause instanceof ConnectException)
            && !(rootCause instanceof UnknownHostException)) {
            // log with full stack trace
            log.log(Level.INFO, msg, e);
        } else {
            // log a one-liner with the root cause
            log.log(Level.INFO, msg + ": " + rootCause.toString());
        }
    }

    public static String humanReadableByteCount(long bytes) {
        if (bytes < 1000) {
            return bytes + " B";
        }
        int exponent = (int)(Math.log(bytes) / Math.log(1000));
        String unit = " kMGTPE".charAt(exponent) + "B";
        return format("%.1f %s", bytes / Math.pow(1000, exponent), unit);
    }

    private static Throwable getRootCause(Throwable t) {
        return t.getCause() == null ? t : getRootCause(t.getCause());
    }
}
