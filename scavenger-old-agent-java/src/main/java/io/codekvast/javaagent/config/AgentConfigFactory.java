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
package io.codekvast.javaagent.config;

import java.io.File;
import java.util.Properties;

import io.codekvast.javaagent.jdk8.Optional;
import io.codekvast.javaagent.util.ConfigUtils;
import io.codekvast.javaagent.util.Constants;
import io.codekvast.javaagent.util.FileUtils;
import io.codekvast.javaagent.util.SignatureUtils;
import lombok.extern.java.Log;

/**
 * A factory for {@link AgentConfig} objects.
 * modified by NAVER: add annotation/constructor filtering option, port code to Java 7
 *
 * @author NAVER
 */
@Log
public class AgentConfigFactory {

    static final String SYSPROP_OPTS = "scavenger.options";
    static final String SYSPROP_ENABLED = "scavenger.enabled";

    private static final String DEFAULT_APP_NAME = "missing-appName";
    private static final String DEFAULT_ASPECTJ_OPTIONS = "";
    private static final boolean DEFAULT_BRIDGE_ASPECTJ_LOGGING_TO_JUL = false;
    private static final String DEFAULT_CODE_BASE = "missing-codeBase";
    private static final String DEFAULT_ENVIRONMENT = "<default>";
    private static final String DEFAULT_EXCLUDE_PACKAGES = "";
    private static final int DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS = 10;
    private static final String DEFAULT_HTTP_PROXY_HOST = null;
    private static final String DEFAULT_HTTP_PROXY_PASSWORD = null;
    private static final int DEFAULT_HTTP_PROXY_PORT = 3128;
    private static final String DEFAULT_HTTP_PROXY_USERNAME = null;
    private static final int DEFAULT_HTTP_READ_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_HTTP_WRITE_TIMEOUT_SECONDS = 30;
    private static final String DEFAULT_METHOD_VISIBILITY = SignatureUtils.PROTECTED;
    private static final String DEFAULT_PACKAGES = "missing-packages";
    private static final String DEFAULT_ADDITIONAL_PACKAGES = "";
    private static final String DEFAULT_ANNOTATIONS = "";
    private static final String DEFAULT_EXCLUDE_CONSTRUCTORS = "false";
    private static final int DEFAULT_SCHEDULER_INITIAL_DELAY_MILLIS = 10_000;
    private static final int DEFAULT_SCHEDULER_INTERVAL_MILLIS = 10_000;
    private static final String DEFAULT_SERVER_URL = "http://localhost:8083";

    private static final String SAMPLE_ASPECTJ_OPTIONS = "-verbose -showWeaveInfo";
    private static final String SAMPLE_CODEBASE_URI1 = "/path/to/codebase1/";
    private static final String SAMPLE_CODEBASE_URI2 = "/path/to/codebase2/";
    private static final String SAMPLE_TAGS = "key1=value1, key2=value2";
    private static final String OVERRIDE_SEPARATOR = ";";
    private static final String UNSPECIFIED = "unspecified";
    private static final String TAGS_KEY = "tags";
    private static final String TRIAL_LICENSE_KEY = "";

    private AgentConfigFactory() {
    }

    static AgentConfig parseAgentConfig(File file, String cmdLineArgs) {
        return parseAgentConfig(file, cmdLineArgs, false);
    }

    public static AgentConfig parseAgentConfig(
        File file, String cmdLineArgs, boolean prependSystemPropertiesToTags) {
        if (file == null) {
            return null;
        }

        if (file.getName().endsWith(".conf.ser")) {
            return FileUtils.deserializeFromFile(file, AgentConfig.class);
        }

        try {
            Properties props = FileUtils.readPropertiesFrom(file);

            parseOverrides(props, System.getProperty(SYSPROP_OPTS));
            parseOverrides(props, cmdLineArgs);
            if (prependSystemPropertiesToTags) {
                doPrependSystemPropertiesToTags(props);
            }

            return buildAgentConfig(props);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse " + file, e);
        }
    }

    private static void parseOverrides(Properties props, String args) {
        if (args != null) {
            String[] overrides = args.split(OVERRIDE_SEPARATOR);
            for (String override : overrides) {
                String[] parts = override.split("=");
                props.setProperty(parts[0].trim(), parts.length < 2 ? "" : parts[1].trim());
            }
        }
    }

    private static AgentConfig buildAgentConfig(Properties props) {

        Optional<String> appName = getMandatoryValue(props, "appName");
        Optional<String> codeBase = getMandatoryValue(props, "codeBase");
        Optional<String> packages = getMandatoryValue(props, "packages");

        boolean defaultEnabled = appName.isPresent() && codeBase.isPresent() && packages.isPresent();
        boolean enabled = ConfigUtils.getBooleanValue(props, "enabled", defaultEnabled);

        Optional<String> rawForceIntervalSeconds = ConfigUtils.getStringValue(props, "forceIntervalSeconds");
        Integer forceIntervalSeconds = null;
        if (rawForceIntervalSeconds.isPresent()) {
            forceIntervalSeconds = Integer.parseInt(rawForceIntervalSeconds.get());
        }

        return AgentConfig.builder()
            .appName(appName.orElse(DEFAULT_APP_NAME))
            .appVersion(ConfigUtils.getStringValue(props, "appVersion", UNSPECIFIED))
            .aspectjOptions(
                ConfigUtils.getStringValue(props, "aspectjOptions", DEFAULT_ASPECTJ_OPTIONS))
            .bridgeAspectjMessagesToJUL(
                ConfigUtils.getBooleanValue(
                    props, "bridgeAspectjMessagesToJUL", DEFAULT_BRIDGE_ASPECTJ_LOGGING_TO_JUL))
            .codeBase(codeBase.orElse(DEFAULT_CODE_BASE))
            .enabled(enabled)
            .environment(ConfigUtils.getStringValue(props, "environment", DEFAULT_ENVIRONMENT))
            .excludePackages(
                ConfigUtils.getStringValue(props, "excludePackages", DEFAULT_EXCLUDE_PACKAGES))
            .httpConnectTimeoutSeconds(
                ConfigUtils.getIntValue(
                    props, "httpConnectTimeoutSeconds", DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS))
            .httpProxyHost(ConfigUtils.getStringValue(props, "httpProxyHost", DEFAULT_HTTP_PROXY_HOST))
            .httpProxyPort(ConfigUtils.getIntValue(props, "httpProxyPort", DEFAULT_HTTP_PROXY_PORT))
            .httpProxyUsername(
                ConfigUtils.getStringValue(props, "httpProxyUsername", DEFAULT_HTTP_PROXY_USERNAME))
            .httpProxyPassword(
                ConfigUtils.getStringValue(props, "httpProxyPassword", DEFAULT_HTTP_PROXY_PASSWORD))
            .httpReadTimeoutSeconds(
                ConfigUtils.getIntValue(
                    props, "httpReadTimeoutSeconds", DEFAULT_HTTP_READ_TIMEOUT_SECONDS))
            .httpWriteTimeoutSeconds(
                ConfigUtils.getIntValue(
                    props, "httpWriteTimeoutSeconds", DEFAULT_HTTP_WRITE_TIMEOUT_SECONDS))
            .hostname(ConfigUtils.getStringValue(props, "hostname", Constants.HOST_NAME))
            .licenseKey(ConfigUtils.getStringValue2(props, "licenseKey", "apiKey", TRIAL_LICENSE_KEY))
            .methodVisibility(
                ConfigUtils.getStringValue(props, "methodVisibility", DEFAULT_METHOD_VISIBILITY))
            .packages(packages.orElse(DEFAULT_PACKAGES))
            .additionalPackages(ConfigUtils.getStringValue(props, "additionalPackages", DEFAULT_ADDITIONAL_PACKAGES))
            .annotations(ConfigUtils.getStringValue(props, "annotations", DEFAULT_ANNOTATIONS))
            .excludeConstructors(ConfigUtils.getStringValue(props, "excludeConstructors", DEFAULT_EXCLUDE_CONSTRUCTORS))
            .serverUrl(ConfigUtils.getStringValue(props, "serverUrl", DEFAULT_SERVER_URL))
            .schedulerInitialDelayMillis(
                ConfigUtils.getIntValue(
                    props, "schedulerInitialDelayMillis", DEFAULT_SCHEDULER_INITIAL_DELAY_MILLIS))
            .schedulerIntervalMillis(
                ConfigUtils.getIntValue(
                    props, "schedulerIntervalMillis", DEFAULT_SCHEDULER_INTERVAL_MILLIS))
            .forceIntervalSeconds(forceIntervalSeconds)
            .tags(ConfigUtils.getStringValue(props, TAGS_KEY, ""))
            .build()
            .validate();
    }

    private static Optional<String> getMandatoryValue(Properties props, String propertyName) {
        Optional<String> value = ConfigUtils.getStringValue(props, propertyName);
        if (!value.isPresent()) {
            log.warning(
                String.format(
                    "The property %s (and system property %s and environment variable %s) is missing",
                    propertyName,
                    ConfigUtils.getSystemPropertyName(propertyName),
                    ConfigUtils.getEnvVarName(propertyName)));
        }
        return value;
    }

    private static void doPrependSystemPropertiesToTags(Properties props) {
        String systemPropertiesTags = createSystemPropertiesTags();

        String oldTags = props.getProperty(TAGS_KEY);
        if (oldTags != null) {
            props.setProperty(TAGS_KEY, systemPropertiesTags + ", " + oldTags);
        } else {
            props.setProperty(TAGS_KEY, systemPropertiesTags);
        }
    }

    private static String createSystemPropertiesTags() {
        String[] sysProps = {
            "java.runtime.name", "java.runtime.version", "os.arch", "os.name", "os.version",
        };

        StringBuilder sb = new StringBuilder();
        String delimiter = "";

        for (String prop : sysProps) {
            String v = System.getProperty(prop);
            if (v != null && !v.isEmpty()) {
                sb.append(delimiter).append(prop).append("=").append(v.replace(",", "\\,"));
                delimiter = ", ";
            }
        }

        return sb.toString();
    }

    public static AgentConfig createSampleAgentConfig() {
        return AgentConfigFactory.createTemplateConfig().toBuilder()
            .appName("Sample Application Name")
            .codeBase(SAMPLE_CODEBASE_URI1 + " , " + SAMPLE_CODEBASE_URI2)
            .packages("com.acme. , foo.bar.")
            .excludePackages("some.excluded.package")
            .annotations("@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service")
            .excludeConstructors("false")
            .build();
    }

    public static AgentConfig createTemplateConfig() {
        return AgentConfig.builder()
            .appName(UNSPECIFIED)
            .appVersion(UNSPECIFIED)
            .aspectjOptions(SAMPLE_ASPECTJ_OPTIONS)
            .bridgeAspectjMessagesToJUL(DEFAULT_BRIDGE_ASPECTJ_LOGGING_TO_JUL)
            .codeBase(UNSPECIFIED)
            .enabled(true)
            .environment(DEFAULT_ENVIRONMENT)
            .excludePackages("")
            .hostname(Constants.HOST_NAME)
            .httpConnectTimeoutSeconds(DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS)
            .httpProxyHost(DEFAULT_HTTP_PROXY_HOST)
            .httpProxyPort(DEFAULT_HTTP_PROXY_PORT)
            .httpProxyUsername(DEFAULT_HTTP_PROXY_USERNAME)
            .httpProxyPassword(DEFAULT_HTTP_PROXY_PASSWORD)
            .httpReadTimeoutSeconds(DEFAULT_HTTP_READ_TIMEOUT_SECONDS)
            .httpWriteTimeoutSeconds(DEFAULT_HTTP_WRITE_TIMEOUT_SECONDS)
            .licenseKey(TRIAL_LICENSE_KEY)
            .methodVisibility(DEFAULT_METHOD_VISIBILITY)
            .packages(UNSPECIFIED)
            .annotations(DEFAULT_ANNOTATIONS)
            .additionalPackages(DEFAULT_ADDITIONAL_PACKAGES)
            .excludeConstructors(DEFAULT_EXCLUDE_CONSTRUCTORS)
            .schedulerInitialDelayMillis(DEFAULT_SCHEDULER_INITIAL_DELAY_MILLIS)
            .schedulerIntervalMillis(DEFAULT_SCHEDULER_INTERVAL_MILLIS)
            .serverUrl(DEFAULT_SERVER_URL)
            .tags(createSystemPropertiesTags() + ", " + SAMPLE_TAGS)
            .build();
    }
}
