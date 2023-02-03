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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.extern.java.Log;

@Log
public class AppVersionResolver {

    private final Collection<AppVersionStrategy> appVersionStrategies = new ArrayList<>();
    private final String version;
    private final List<File> codeBaseFiles;

    public AppVersionResolver(String version, List<File> codeBaseFiles) {
        this.version = version.trim();
        this.codeBaseFiles = new ArrayList<>(codeBaseFiles);

        this.appVersionStrategies.add(new LiteralAppVersionStrategy());
        this.appVersionStrategies.add(new ManifestAppVersionStrategy());
        this.appVersionStrategies.add(new FilenameAppVersionStrategy());
        this.appVersionStrategies.add(new PropertiesAppVersionStrategy());
    }

    public static boolean isUnresolved(String appVersion) {
        return appVersion == null || appVersion.equals(AppVersionStrategy.UNKNOWN_VERSION);
    }

    public String resolveAppVersion() {
        String[] args = version.split("\\s+");

        for (AppVersionStrategy strategy : appVersionStrategies) {
            if (strategy.canHandle(args)) {
                String resolvedVersion = strategy.resolveAppVersion(codeBaseFiles, args);
                log.info(
                    String.format(
                        "%s resolved appVersion '%s' to '%s'",
                        strategy.getClass().getSimpleName(), version, resolvedVersion));
                return resolvedVersion;
            }
        }

        log.info(
            String.format("Don't know how to resolve appVersion '%s', using it as-is", version));
        return version;
    }
}
