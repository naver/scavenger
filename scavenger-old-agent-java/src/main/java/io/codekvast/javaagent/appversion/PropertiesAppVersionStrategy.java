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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Properties;

import lombok.extern.java.Log;

/**
 * A strategy for picking the app version from one or more properties in a properties file.
 *
 * <p>It handles the cases {@code properties somefile.conf prop1[,prop2...]}. The values of
 * properties (separated by a dash '-') are used as resolved version.
 *
 * @author olle.hallin@crisp.se
 */
@Log
public class PropertiesAppVersionStrategy extends AbstractAppVersionStrategy {
    PropertiesAppVersionStrategy() {
        super("properties", "property");
    }

    @Override
    public boolean canHandle(String[] args) {
        return args != null && args.length >= 3 && recognizes(args[0]);
    }

    @Override
    public String resolveAppVersion(Collection<File> codeBases, String[] args) {

        // Try to read directly from the file
        File file = new File(args[1]);
        if (file.canRead()) {
            return getVersionFrom(file, args);
        }

        // Else locate it within codeBases
        String baseName = args[1];
        for (File codeBaseFile : codeBases) {
            String version = search(codeBaseFile, baseName, args);
            if (version != null) {
                return version;
            }
        }
        log.severe(String.format("Cannot resolve '%s': file not found", join(args)));
        return UNKNOWN_VERSION;
    }

    private String search(File dir, String baseName, String[] args) {
        if (!dir.isDirectory()) {
            log.warning(dir + " is not a directory");
            return null;
        }

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals(baseName)) {
                    String version = getVersionFrom(file, args);
                    log.fine(String.format("Found version '%s' in %s", version, file));
                    return version;
                }
                if (file.isDirectory()) {
                    String version = search(file, baseName, args);
                    if (version != null) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    String getVersionFrom(File file, String[] args) {
        Properties props = new Properties();

        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            props.load(is);
        } catch (IOException e) {
            log.severe("Cannot load " + file + ": " + e.getMessage());
            return UNKNOWN_VERSION;
        }

        StringBuilder sb = new StringBuilder();
        String delimiter = "";

        for (int i = 2; i < args.length; i++) {
            String key = args[i];
            String value = props.getProperty(key);
            if (value == null) {
                log.warning("Cannot find " + key + " in " + file.getAbsolutePath());
            } else {
                sb.append(delimiter).append(removeQuotes(value).trim());
                delimiter = "-";
            }
        }
        return sb.toString();
    }

    private String removeQuotes(String value) {
        int len = value.length();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, len - 1);
        }
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, len - 1);
        }
        return value;
    }

    private String join(String[] args) {
        StringBuilder sb = new StringBuilder();
        String delimiter = "";
        for (String arg : args) {
            sb.append(delimiter).append(arg);
            delimiter = " ";
        }
        return sb.toString();
    }
}
