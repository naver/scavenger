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

import java.io.Serializable;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Representation of a code base entry.
 * modified by NAVER: bump up version to 4
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@Value
@Builder(toBuilder = true)
public class CodeBaseEntry4 implements Serializable {
    private static final long serialVersionUID = 4L;

    /**
     * The low-level description of the signature.
     */
    MethodSignature4 methodSignature;

    /**
     * The visibility of the signature. Package private is coded as 'package-private'.
     */
    @NonNull
    String visibility;

    /**
     * The signature.
     */
    @NonNull
    String signature;

    public static CodeBaseEntry4 sampleCodeBaseEntry() {
        return builder()
            .methodSignature(MethodSignature4.createSampleMethodSignature())
            .signature("signature1()")
            .visibility("public")
            .build();
    }
}
