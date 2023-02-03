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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import lombok.extern.java.Log;

/**
 * A strategy for picking the app version from a jar manifest.
 *
 * <p>It handles the cases {@code manifest some.jar} and {@code manifest some.jar
 * My-Custom-Manifest-Attribute} where some.jar can be either an URI or a file path.
 *
 * <p>The two-arg version uses {@code Implementation-Version} as manifest attribute.
 *
 * @author olle.hallin@crisp.se
 */
@Log
public class ManifestAppVersionStrategy extends AbstractAppVersionStrategy {

    private static final String DEFAULT_MANIFEST_ATTRIBUTE = "Implementation-Version";

    ManifestAppVersionStrategy() {
        super("manifest", "search");
    }

    @Override
    public boolean canHandle(String[] args) {
        return args != null && (args.length == 2 || args.length == 3) && recognizes(args[0]);
    }

    @Override
    public String resolveAppVersion(Collection<File> codeBases, String[] args) {
        String jarUri = args[1];
        String manifestAttribute = args.length > 2 ? args[2] : DEFAULT_MANIFEST_ATTRIBUTE;
        for (File codeBaseFile : codeBases) {
            try (JarFile jarFile = new JarFile(getJarFile(codeBaseFile, jarUri))) {
                Attributes attributes = jarFile.getManifest().getMainAttributes();
                String resolvedVersion = attributes.getValue(manifestAttribute);
                if (resolvedVersion != null) {
                    log.fine(
                        String.format(
                            "%s!/META-INF/MANIFEST.MF:%s=%s", jarUri, manifestAttribute, resolvedVersion));
                    return resolvedVersion;
                }
                if (!manifestAttribute.equalsIgnoreCase(DEFAULT_MANIFEST_ATTRIBUTE)) {
                    resolvedVersion = attributes.getValue(DEFAULT_MANIFEST_ATTRIBUTE);
                }
                if (resolvedVersion != null) {
                    log.fine(
                        String.format(
                            "%s!/META-INF/MANIFEST.MF:%s=%s",
                            jarUri, DEFAULT_MANIFEST_ATTRIBUTE, resolvedVersion));
                    return resolvedVersion;
                }
            } catch (Exception e) {
                log.info("Cannot open " + jarUri + ": " + e);
            }
        }
        log.info(
            String.format("Cannot resolve %s!/META-INF/MANIFEST.MF:%s", jarUri, manifestAttribute));
        return UNKNOWN_VERSION;
    }

    private File getJarFile(File codeBaseFile, String jarUri) throws IOException, URISyntaxException {
        URL url = null;
        // try to parse it as a URL...
        try {
            url = new URL(jarUri);
        } catch (MalformedURLException ignore) {
            // Ignore
        }

        if (url == null) {
            // Try to treat it as a file...
            File file = new File(jarUri);
            if (file.isFile() && file.canRead() && file.getName().endsWith(".jar")) {
                url = file.toURI().toURL();
            }
        }
        if (url == null) {
            // Search for it in codeBaseFile. Treat it as a regular expression for the basename
            url = search(codeBaseFile, jarUri);
        }

        File result = url == null ? null : new File(url.toURI());
        if (result == null) {
            throw new IOException("Cannot find " + jarUri);
        }
        if (!result.canRead()) {
            throw new IOException("Cannot read " + jarUri);
        }
        return result;
    }

    private URL search(File dir, String regex) throws MalformedURLException {
        if (!dir.isDirectory()) {
            log.info(dir + " is not a directory");
            return null;
        }

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().matches(regex)) {
                    log.fine("Found " + file);
                    return new URL(file.toURI().toString());
                }
                if (file.isDirectory()) {
                    URL url = search(file, regex);
                    if (url != null) {
                        return url;
                    }
                }
            }
        }
        return null;
    }
}
