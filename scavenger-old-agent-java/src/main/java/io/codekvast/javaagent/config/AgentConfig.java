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

import static io.codekvast.javaagent.model.Endpoints.Agent.V4_INIT_CONFIG;
import static io.codekvast.javaagent.model.Endpoints.Agent.V4_POLL_CONFIG;
import static io.codekvast.javaagent.model.Endpoints.Agent.V4_UPLOAD_CODEBASE;
import static io.codekvast.javaagent.model.Endpoints.Agent.V4_UPLOAD_INVOCATION_DATA;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.google.gson.Gson;
import io.codekvast.javaagent.appversion.AppVersionResolver;
import io.codekvast.javaagent.model.v4.CommonPublicationData4;
import io.codekvast.javaagent.model.v4.InitConfigResponse4;
import io.codekvast.javaagent.util.ConfigUtils;
import io.codekvast.javaagent.util.Constants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Encapsulates the configuration that is used by the Codekvast agent.
 * modified by NAVER: add annotation/constructor filtering option, fetch collectorUrl dynamically
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@Log
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyComplexClass"})
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class AgentConfig implements Serializable {
    public static final String INVOCATIONS_BASENAME = "invocations.dat";
    public static final String JVM_BASENAME = "jvm.dat";
    private static final long serialVersionUID = 1L;
    private boolean enabled;

    @NonNull
    private String licenseKey;

    @NonNull
    private String serverUrl;

    @NonNull
    private String aspectjOptions;

    private boolean bridgeAspectjMessagesToJUL;

    @NonNull
    private String methodVisibility;

    @NonNull
    private String appName;

    @NonNull
    private String appVersion;

    @NonNull
    private String codeBase;

    @NonNull
    private String environment;

    @NonNull
    private String packages;

    @NonNull
    private String additionalPackages;

    @NonNull
    private String excludePackages;

    @NonNull
    private String annotations;

    @NonNull
    private String excludeConstructors;

    @NonNull
    private String tags;

    @NonNull
    private String hostname;

    private int httpConnectTimeoutSeconds;
    private int httpReadTimeoutSeconds;
    private int httpWriteTimeoutSeconds;
    private String httpProxyHost;
    private int httpProxyPort;
    private String httpProxyUsername;
    private String httpProxyPassword;
    private int schedulerInitialDelayMillis;
    private int schedulerIntervalMillis;
    private Integer forceIntervalSeconds;

    private String resolvedAppVersion;

    private transient OkHttpClient httpClient;

    private Boolean boolExcludeConstructors;

    private List<String> separatedPackages;
    private List<String> separatedAnnotations;
    private List<String> separatedAdditionalPackages;
    private List<String> separatedExcludePackages;

    private String collectorUrl;

    private String getCollectorUrl() {
        if (collectorUrl == null) {
            String initConfigUrl = getInitConfigRequestEndpoint();
            log.info("[scavenger] trying to resolve collector url by accessing " + initConfigUrl);
            HttpUrl url = HttpUrl.parse(initConfigUrl).newBuilder()
                .addQueryParameter("licenseKey", getLicenseKey())
                .build();
            Request request = new Request.Builder().url(url).build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException(response.body().string());
                }

                String body = response.body().string();
                InitConfigResponse4 initConfigResponse4 = new Gson().fromJson(body, InitConfigResponse4.class);
                collectorUrl = initConfigResponse4.getCollectorUrl();
                log.info("[scavenger] resolved collector url is " + collectorUrl);
                return collectorUrl;
            } catch (IOException e) {
                log.log(Level.SEVERE, "[scavenger] fail to resolve collector url by accessing " + initConfigUrl, e);
                throw new RuntimeException(e);
            }
        }
        return collectorUrl;
    }

    public List<String> getSeparatedPackages() {
        if (separatedPackages == null) {
            separatedPackages = ConfigUtils.getSeparatedValues(packages);
        }
        return separatedPackages;
    }

    public List<String> getSeparatedAdditionalPackages() {
        if (separatedAdditionalPackages == null) {
            separatedAdditionalPackages = ConfigUtils.getSeparatedValues(additionalPackages);
        }
        return separatedAdditionalPackages;
    }

    public List<String> getSeparatedExcludePackages() {
        if (separatedExcludePackages == null) {
            separatedExcludePackages = ConfigUtils.getSeparatedValues(excludePackages);
        }
        return separatedExcludePackages;
    }

    public List<String> getSeparatedAnnotations() {
        if (separatedAnnotations == null) {
            separatedAnnotations = ConfigUtils.getSeparatedValues(annotations);
        }
        return separatedAnnotations;
    }

    public boolean getExcludeConstructors() {
        if (boolExcludeConstructors == null) {
            boolExcludeConstructors = ConfigUtils.getBooleanFromStringValue(excludeConstructors);
        }
        return boolExcludeConstructors;
    }

    public List<File> getCodeBaseFiles() {
        return ConfigUtils.getCommaSeparatedFileValues(codeBase);
    }

    public MethodAnalyzer getMethodAnalyzer() {
        return new MethodAnalyzer(this.methodVisibility);
    }

    public String getInitConfigRequestEndpoint() {
        return String.format("%s%s", serverUrl, V4_INIT_CONFIG);
    }

    public String getPollConfigRequestEndpoint() {
        return String.format("%s%s", getCollectorUrl(), V4_POLL_CONFIG);
    }

    public String getCodeBaseUploadEndpoint() {
        return String.format("%s%s", getCollectorUrl(), V4_UPLOAD_CODEBASE);
    }

    public String getInvocationDataUploadEndpoint() {
        return String.format("%s%s", getCollectorUrl(), V4_UPLOAD_INVOCATION_DATA);
    }

    public String getResolvedAppVersion() {
        if (AppVersionResolver.isUnresolved(resolvedAppVersion)) {
            resolvedAppVersion =
                new AppVersionResolver(this.getAppVersion(), this.getCodeBaseFiles()).resolveAppVersion();
        }
        return resolvedAppVersion;
    }

    public OkHttpClient getHttpClient() {
        if (httpClient == null) {
            validate();
            OkHttpClient.Builder builder =
                new OkHttpClient.Builder()
                    .connectTimeout(httpConnectTimeoutSeconds, TimeUnit.SECONDS)
                    .writeTimeout(httpWriteTimeoutSeconds, TimeUnit.SECONDS)
                    .readTimeout(httpReadTimeoutSeconds, TimeUnit.SECONDS);

            Proxy proxy = createHttpProxy();
            if (proxy != null) {
                builder.proxy(proxy);
            }

            Authenticator proxyAuthenticator = createProxyAuthenticator();
            if (proxyAuthenticator != null) {
                builder.proxyAuthenticator(proxyAuthenticator);
            }
            httpClient = builder.build();
        }
        return httpClient;
    }

    private Proxy createHttpProxy() {
        if (httpProxyHost == null || httpProxyHost.trim().isEmpty()) {
            return null;
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));
    }

    private Authenticator createProxyAuthenticator() {
        if (httpProxyHost == null || httpProxyHost.trim().isEmpty()) {
            return null;
        }

        if (httpProxyUsername == null || httpProxyUsername.trim().isEmpty()) {
            return null;
        }

        return new ProxyAuthenticator();
    }

    public String getFilenamePrefix(@NonNull String prefix) {
        String result =
            String.format("%s-%s-%s-", prefix.replaceAll("-+$", ""), appName, getResolvedAppVersion());
        return result.toLowerCase().replaceAll("[^a-z0-9._+-]", "");
    }

    public CommonPublicationData4 commonPublicationData() {
        return CommonPublicationData4.builder()
            .appName(getAppName())
            .appVersion(getResolvedAppVersion())
            .agentVersion(Constants.AGENT_VERSION)
            .computerId(Constants.COMPUTER_ID)
            .environment(getEnvironment())
            .excludePackages(getSeparatedExcludePackages())
            .hostname(getHostname())
            .jvmStartedAtMillis(Constants.JVM_STARTED_AT_MILLIS)
            .jvmUuid(Constants.JVM_UUID)
            .methodVisibility(getNormalizedMethodVisibility())
            .packages(getSeparatedPackages())
            .publishedAtMillis(System.currentTimeMillis())
            .tags(getTags())
            .codeBaseFingerprint("to-be-replaced")
            .build();
    }

    private String getNormalizedMethodVisibility() {
        return getMethodAnalyzer().toString();
    }

    AgentConfig validate() {
        if (httpProxyPort <= 0) {
            throw new IllegalArgumentException(
                "Illegal httpProxyPort " + httpProxyPort + ": must be a positive integer");
        }
        return this;
    }

    private class ProxyAuthenticator implements Authenticator {
        @Override
        public Request authenticate(Route route, Response response) {
            String credential =
                Credentials.basic(httpProxyUsername, httpProxyPassword == null ? "" : httpProxyPassword);
            return response.request().newBuilder().header("Proxy-Authorization", credential).build();
        }
    }
}
