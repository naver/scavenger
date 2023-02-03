/*
 * Copyright (c) 2023-present NAVER Corp.
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

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.google.common.base.Strings;
import io.codekvast.javaagent.jdk8.StringUtils;
import io.codekvast.javaagent.jdk8.Supplier;
import lombok.extern.java.Log;

/**
 * @author NAVER
 */
@Log
public class FileLogger {
    private static FileWriter fileWriter = null;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        String logFilePath = System.getProperty("scavenger.log-path");
        log.info("[scavenger] scavenger.log-path is " + logFilePath);
        if (!Strings.isNullOrEmpty(logFilePath)) {
            try {
                fileWriter = new FileWriter(logFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void log(Supplier<String> message) {
        if (fileWriter != null) {
            try {
                fileWriter.write("[" + format.format(new Date()) + "]  ");
                fileWriter.write(message.get());
                fileWriter.write("\n");
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void log(final String... strings) {
        if (fileWriter != null) {
            log(
                new Supplier<String>() {
                    @Override
                    public String get() {
                        return StringUtils.join("", Arrays.asList(strings));
                    }
                }
            );
        }
    }

    public static synchronized void log(final String header, final Object object) {
        if (fileWriter != null) {
            log(
                new Supplier<String>() {
                    @Override
                    public String get() {
                        return header + object;
                    }
                }
            );
        }
    }

    public static synchronized void log(final String header, final String delimiter, final Collection<String> values) {
        if (fileWriter != null) {
            log(header, StringUtils.join(delimiter, values));
        }
    }
}
