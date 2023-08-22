package com.navercorp.scavenger.javaagent.model;

import static com.navercorp.scavenger.javaagent.util.ConfigUtils.getAliasedStringValue;
import static com.navercorp.scavenger.javaagent.util.ConfigUtils.getBooleanValue;
import static com.navercorp.scavenger.javaagent.util.ConfigUtils.getIntValue;
import static com.navercorp.scavenger.javaagent.util.ConfigUtils.getSeparatedValues;
import static com.navercorp.scavenger.javaagent.util.ConfigUtils.getStringValue;
import static com.navercorp.scavenger.javaagent.util.ConfigUtils.getVisibilityValue;
import static com.navercorp.scavenger.javaagent.util.ConfigUtils.separateValues;
import static com.navercorp.scavenger.javaagent.util.ConfigUtils.withEndingDot;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.util.Constants;
import com.navercorp.scavenger.model.CommonPublicationData;

@Data
@Log
public class Config {
    private boolean enabled = true;
    private String location = null;

    private String apiKey = "";
    private String serverUrl = "http://localhost:8080";
    private String appName = "missing-appName";
    private List<String> codeBase;
    private List<String> packages;
    private List<String> excludePackages;
    private List<String> additionalPackages;
    private List<String> annotations;
    private Visibility methodVisibility = Visibility.PROTECTED;
    private boolean excludeConstructors = false;
    private boolean excludeGetterSetter = false;
    private String environment = "<default>";
    private String appVersion = "unspecified";
    private String hostname = Constants.HOST_NAME;

    private int httpConnectTimeoutSeconds = 10;
    private int httpReadTimeoutSeconds = 10;
    private int httpWriteTimeoutSeconds = 30;

    private int schedulerInitialDelayMillis = 10_000;
    private int schedulerIntervalMillis = 10_000;
    private Integer forceIntervalSeconds;
    private Integer maxMethodsCount;

    private boolean asyncCodeBaseScanMode = false;
    private boolean legacyCompatibilityMode = false;
    private boolean debugMode = false;

    public Config(Properties props) {
        enabled = getBooleanValue(props, "enabled", enabled);
        location = getStringValue(props, "location", location);

        appName = getMandatoryValue(props, "appName", appName);
        packages = separateValues(getMandatoryValue(props, "packages", null));
        codeBase = getSeparatedValues(props, "codeBase");
        apiKey = getAliasedStringValue(props, "apiKey", "licenseKey", apiKey);
        serverUrl = getStringValue(props, "serverUrl", serverUrl);
        excludePackages = getSeparatedValues(props, "excludePackages");
        additionalPackages = getSeparatedValues(props, "additionalPackages");
        annotations = getSeparatedValues(props, "annotations").stream()
            .map(it -> it.startsWith("@") ? it.substring(1) : it)
            .collect(Collectors.toList());
        methodVisibility = getVisibilityValue(props, "methodVisibility", methodVisibility);
        excludeConstructors = getBooleanValue(props, "excludeConstructors", excludeConstructors);
        excludeGetterSetter = getBooleanValue(props, "excludeGetterSetter", excludeGetterSetter);
        environment = getStringValue(props, "environment", environment);
        appVersion = getStringValue(props, "appVersion", appVersion);
        hostname = getStringValue(props, "hostname", hostname);

        httpConnectTimeoutSeconds = getIntValue(props, "httpConnectTimeoutSeconds", httpConnectTimeoutSeconds);
        httpReadTimeoutSeconds = getIntValue(props, "httpReadTimeoutSeconds", httpReadTimeoutSeconds);
        httpWriteTimeoutSeconds = getIntValue(props, "httpWriteTimeoutSeconds", httpWriteTimeoutSeconds);

        schedulerInitialDelayMillis = getIntValue(props, "schedulerInitialDelayMillis", schedulerInitialDelayMillis);
        schedulerIntervalMillis = getIntValue(props, "schedulerIntervalMillis", schedulerIntervalMillis);
        forceIntervalSeconds = getIntValue(props, "forceIntervalSeconds", 0);
        maxMethodsCount = getIntValue(props, "maxMethodsCount", 100000);

        asyncCodeBaseScanMode = getBooleanValue(props, "asyncCodeBaseScanMode", asyncCodeBaseScanMode);
        legacyCompatibilityMode = getBooleanValue(props, "legacyCompatibilityMode", legacyCompatibilityMode);
        debugMode = getBooleanValue(props, "debugMode", debugMode);
    }

    public List<String> getPackagesWithEndingDot() {
        return withEndingDot(packages);
    }

    public List<String> getExcludePackagesWithEndingDot() {
        return withEndingDot(excludePackages);
    }

    public List<String> getAdditionalPackagesWithEndingDot() {
        return withEndingDot(additionalPackages);
    }

    public CommonPublicationData buildCommonPublicationData() {
        return CommonPublicationData.newBuilder()
            .setAppName(getAppName())
            .setAppVersion(getAppVersion())
            .setEnvironment(getEnvironment())
            .setHostname(getHostname())
            .setJvmStartedAtMillis(Constants.JVM_STARTED_AT_MILLIS)
            .setJvmUuid(Constants.JVM_UUID)
            .setPublishedAtMillis(System.currentTimeMillis())
            .setCodeBaseFingerprint("to-be-replaced")
            .setApiKey(getApiKey())
            .build();
    }

    private String getMandatoryValue(Properties props, String key, String defaultValue) {
        String value = getStringValue(props, key, null);

        if (value == null) {
            enabled = false;
            value = defaultValue;

            log.warning(String.format("[scavenger] mandatory property '%s' is missing", key));
        }

        return value;
    }
}
