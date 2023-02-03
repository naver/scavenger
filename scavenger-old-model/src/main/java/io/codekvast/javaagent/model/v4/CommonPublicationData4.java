/*
 * Copyright (c) 2015-2022 Hallin Information Technology AB
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
package io.codekvast.javaagent.model.v4;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * modified by NAVER: bump up version to 4
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyComplexClass"})
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class CommonPublicationData4 implements Serializable {
    private static final long serialVersionUID = 4L;

    @Min(value = 1, message = "customerId must be a positive number")
    private long customerId;

    @NonNull
    @Size(min = 1)
    private String appName;

    @NonNull
    @Size(min = 1)
    private String appVersion;

    @NonNull
    @Size(min = 1)
    private String codeBaseFingerprint;

    @NonNull
    @Size(min = 1)
    private String agentVersion;

    @NonNull
    @Size(min = 1)
    private String computerId;

    @NonNull
    private String environment;

    @NonNull
    private List<String> excludePackages;

    @NonNull
    @Size(min = 1)
    private String hostname;

    @Min(1_490_000_000_000L)
    private long jvmStartedAtMillis;

    @NonNull
    @Size(min = 1)
    private String jvmUuid;

    @NonNull
    @Size(min = 1)
    private String methodVisibility;

    @NonNull
    private List<String> packages;

    @Min(1_490_000_000_000L)
    private long publishedAtMillis;

    @Min(1)
    private int sequenceNumber;

    @NonNull
    private String tags;

    public static CommonPublicationData4 sampleCommonPublicationData() {
        return builder()
            .agentVersion("agentVersion")
            .appName("appName")
            .appVersion("appVersion")
            .codeBaseFingerprint("codeBaseFingerprint")
            .computerId("computerId")
            .customerId(1L)
            .environment("environment")
            .excludePackages(asList("excludePackages1", "excludePackages2"))
            .hostname("hostname")
            .jvmStartedAtMillis(1509461136162L)
            .jvmUuid("jvmUuid")
            .methodVisibility("methodVisibility")
            .packages(asList("packages1", "packages2"))
            .publishedAtMillis(1509461136162L)
            .sequenceNumber(1)
            .tags("tags")
            .build();
    }

    @Override
    public String toString() {
        return String.format(
            "%1$s(customerId=%2$d, appName='%3$s', appVersion='%4$s', hostname='%5$s', publishedAt=%6$tF:%6$tT%6$tz)",
            this.getClass().getSimpleName(),
            customerId,
            appName,
            appVersion,
            hostname,
            publishedAtMillis);
    }

    public String toFullString() {
        return String.format(
            "%1$s(" +
                "customerId=%2$d, " +
                "appName=%3$s, " +
                "appVersion=%4$s, " +
                "environment=%5$s, " +
                "jvmUuid=%6$s, " +
                "codeBaseFingerprint=%7$s, " +
                "publishedAtMillis=%8$d, " +
                "jvmStartedAtMillis=%9$d, " +
                "methodVisibility=%10$s, " +
                "packages=%11$s, " +
                "excludePackages=%12$s, " +
                "computerId=%13$s, " +
                "hostname=%14$s, " +
                "agentVersion=%15$s, " +
                "sequenceNumber=%16$d, " +
                "tags=%17$s)",
            this.getClass().getSimpleName(),
            customerId,
            appName,
            appVersion,
            environment,
            jvmUuid,
            codeBaseFingerprint,
            publishedAtMillis,
            jvmStartedAtMillis,
            methodVisibility,
            packages,
            excludePackages,
            computerId,
            hostname,
            agentVersion,
            sequenceNumber,
            tags
        );
    }
}
