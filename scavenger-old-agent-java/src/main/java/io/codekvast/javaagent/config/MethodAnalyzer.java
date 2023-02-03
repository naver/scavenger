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

import static io.codekvast.javaagent.util.SignatureUtils.PACKAGE_PRIVATE;
import static io.codekvast.javaagent.util.SignatureUtils.PRIVATE;
import static io.codekvast.javaagent.util.SignatureUtils.PROTECTED;
import static io.codekvast.javaagent.util.SignatureUtils.PUBLIC;

import lombok.EqualsAndHashCode;

/**
 * @author olle.hallin@crisp.se
 */
@EqualsAndHashCode
public class MethodAnalyzer {

    private boolean selectsPublic = false;
    private boolean selectsProtected = false;
    private boolean selectsPrivate = false;
    private boolean selectsPackagePrivate = false;

    public MethodAnalyzer(String visibility) {
        boolean recognized = false;

        String value = visibility == null ? PUBLIC : visibility.trim().toLowerCase();
        if (value.equals(PUBLIC)) {
            selectsPublic = true;
            recognized = true;
        }
        if (value.equals(PROTECTED)) {
            selectsPublic = true;
            selectsProtected = true;
            recognized = true;
        }

        if (value.equals(PACKAGE_PRIVATE) || value.equals("!private")) {
            selectsPublic = true;
            selectsProtected = true;
            selectsPackagePrivate = true;
            recognized = true;
        }

        if (value.equals(PRIVATE) || value.equals("all")) {
            selectsPublic = true;
            selectsProtected = true;
            selectsPackagePrivate = true;
            selectsPrivate = true;
            recognized = true;
        }

        if (!recognized) {
            if (!value.isEmpty()) {
                //noinspection UseOfSystemOutOrSystemErr
                System.err.println(
                    "Unrecognized value for methodVisibility: \"" + value + "\", assuming \"public\"");
            }
            selectsPublic = true;
        }
    }

    public boolean selectsPublicMethods() {
        return selectsPublic;
    }

    public boolean selectsProtectedMethods() {
        return selectsProtected;
    }

    public boolean selectsPackagePrivateMethods() {
        return selectsPackagePrivate;
    }

    public boolean selectsPrivateMethods() {
        return selectsPrivate;
    }

    @Override
    public String toString() {
        if (selectsPrivateMethods()) {
            return PRIVATE;
        }
        if (selectsPackagePrivateMethods()) {
            return PACKAGE_PRIVATE;
        }
        if (selectsProtectedMethods()) {
            return PROTECTED;
        }
        return PUBLIC;
    }
}
