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
package io.codekvast.javaagent.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.Factory;

import com.google.common.annotations.VisibleForTesting;
import io.codekvast.javaagent.jdk8.StringUtils;
import io.codekvast.javaagent.model.v4.MethodLocation4;
import io.codekvast.javaagent.model.v4.MethodSignature4;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import lombok.val;

/**
 * Utility class for dealing with signatures.
 * modified by NAVER: port code to Java 7
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@UtilityClass
@Log
public class SignatureUtils {

    public static final String PUBLIC = "public";
    public static final String PROTECTED = "protected";
    public static final String PACKAGE_PRIVATE = "package-private";
    public static final String PRIVATE = "private";
    private static final String[] VISIBILITY_KEYWORDS = {PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE};

    public static String normalizeSignature(MethodSignature4 methodSignature) {
        return methodSignature == null ? null : normalizeSignature(methodSignature.getAspectjString());
    }

    public static String normalizeSignature(String signature) {
        return signature == null ? null : signature.replace(" final ", " ");
    }

    /**
     * Converts a (method) signature to a string containing the bare minimum to uniquely identify the
     * method, namely:
     *
     * <ul>
     *   <li>The declaring class name
     *   <li>The method name
     *   <li>The full parameter types
     *   <li>The source location
     * </ul>
     *
     * @param signature The signature to convert. May be null.
     * @param location  The source location (file name). May be null.
     * @return A string representation of the signature appended by " (location)" or null.
     */
    public static MethodLocation4 makeMethodLocation(Signature signature, String location) {
        if (signature == null) {
            return null;
        }
        return new MethodLocation4(signature.toLongString(), location);
    }

    public static String signatureToString(Signature signature) {
        return signature == null ? null : signature.toLongString();
    }

    public static String stripModifiers(String signature) {
        // Search backwards from the '(' for a space character...
        int pos = signature.indexOf("(");
        if (pos < 0) {
            // Constructor
            pos = signature.length() - 1;
        }
        while (pos >= 0 && signature.charAt(pos) != ' ' ) {
            pos -= 1;
        }
        return signature.substring(pos + 1);
    }

    static String stripModifiersAndReturnType(String signature) {
        if (signature == null) {
            return null;
        }
        return getVisibility(signature) + " " + stripModifiers(signature);
    }

    public static String getVisibility(String modifiers) {
        for (String v : VISIBILITY_KEYWORDS) {
            if (modifiers.contains(v)) {
                return v;
            }
        }
        return PACKAGE_PRIVATE;
    }

    /**
     * Uses AspectJ for creating the same signature as AbstractCodekvastAspect.
     *
     * @param clazz  The class containing the method
     * @param method The method to make a signature of
     * @return The same signature object as an AspectJ execution pointcut will provide in
     * JoinPoint.getSignature(). Returns null unless the method is not synthetic.
     */
    public static Signature makeSignature(Class<?> clazz, Method method) {
        if (clazz == null || method.isSynthetic()) {
            return null;
        }

        return new Factory(makeLocation(clazz), clazz)
            .makeMethodSig(
                method.getModifiers(),
                method.getName(),
                clazz,
                method.getParameterTypes(),
                null,
                method.getExceptionTypes(),
                method.getReturnType());
    }

    /**
     * Uses AspectJ for creating the same signature as AbstractCodekvastAspect.
     *
     * @param clazz       The class containing the method
     * @param constructor The constructor to make a signature of
     * @return The same signature object as an AspectJ execution pointcut will provide in
     * JoinPoint.getSignature(). Returns null unless the constructor is not synthetic.
     */
    private static Signature makeSignature(Class<?> clazz, Constructor<?> constructor) {
        if (clazz == null || constructor.isSynthetic()) {
            return null;
        }

        return new Factory(makeLocation(clazz), clazz)
            .makeConstructorSig(
                constructor.getModifiers(),
                clazz,
                constructor.getParameterTypes(),
                null,
                constructor.getExceptionTypes());
    }

    /**
     * Converts a java.lang.reflect.Method to a MethodSignature4 object.
     *
     * @param clazz  The class containing the method
     * @param method The method to make a signature of
     * @return A MethodSignature3 or null if the methodFilter stops the method.
     * @see #makeSignature(Class, Method)
     */
    public static MethodSignature4 makeMethodSignature(Class<?> clazz, Method method) {
        org.aspectj.lang.reflect.MethodSignature aspectjSignature =
            (org.aspectj.lang.reflect.MethodSignature)makeSignature(clazz, method);

        if (aspectjSignature == null) {
            return null;
        }

        MethodLocation4 methodLocation = makeMethodLocation(aspectjSignature, makeLocation(clazz));

        return MethodSignature4.builder()
            .aspectjString(stripModifiersAndReturnType(methodLocation.getSignature()))
            .bridge(method.isBridge())
            .declaringType(aspectjSignature.getDeclaringTypeName())
            .exceptionTypes(classArrayToString(aspectjSignature.getExceptionTypes()))
            .methodName(aspectjSignature.getName())
            .modifiers(Modifier.toString(aspectjSignature.getModifiers()))
            .packageName(aspectjSignature.getDeclaringType().getPackage().getName())
            .parameterTypes(classArrayToString(aspectjSignature.getParameterTypes()))
            .returnType(aspectjSignature.getReturnType().getName())
            .synthetic(method.isSynthetic())
            .location(methodLocation.getLocation())
            .annotations(buildAnnotations(clazz, method.getName(), method.getDeclaredAnnotations()))
            .build();
    }

    List<String> buildAnnotations(Class<?> clazz, String methodName, Annotation[] methodAnnotations) {
        FileLogger.log("*** build annotations from ", clazz.getPackage().toString(), " ", clazz.getName(), " / ", methodName);

        List<String> annotations = new ArrayList<>();
        for (Annotation classAnnotation : clazz.getDeclaredAnnotations()) {
            annotations.add(classAnnotation.toString());
        }
        for (Annotation methodAnnotation : methodAnnotations) {
            annotations.add(methodAnnotation.toString());
        }

        FileLogger.log("*** constructed ", ", ", annotations);
        return annotations;
    }

    @VisibleForTesting
    static String makeLocation(Class<?> clazz) {
        try {
            ProtectionDomain protectionDomain = clazz.getProtectionDomain();
            if (protectionDomain != null) {
                CodeSource codeSource = protectionDomain.getCodeSource();
                if (codeSource != null) {
                    URL location = codeSource.getLocation();
                    if (location != null) {
                        String loc = location.toString();
                        if (loc.endsWith(".jar") || loc.endsWith(".zip")) {
                            int pos = loc.lastIndexOf("/");
                            return loc.substring(pos + 1);
                        }
                        for (String suffix :
                            Arrays.asList("BOOT-INF/classes/", "WEB-INF/classes/", "classes/")) {
                            if (loc.endsWith(suffix)) {
                                return suffix;
                            }
                        }

                        // Probably in dev environment, make location relative to $PWD
                        val pwd = "file:" + System.getProperty("user.dir") + "/";
                        return loc.replace(pwd, "");
                    }
                }
            }
        } catch (SecurityException ignore) {
            // ignore
        }
        return null;
    }

    /**
     * Converts a java.lang.reflect.Constructor to a MethodSignature4 object.
     *
     * @param clazz       The class containing the method.
     * @param constructor The constructor to make a signature of.
     * @return A MethodSignature3 or null if the methodFilter stops the constructor.
     * @see #makeSignature(Class, Method)
     */
    public static MethodSignature4 makeConstructorSignature(
        Class<?> clazz, Constructor<?> constructor) {
        org.aspectj.lang.reflect.ConstructorSignature aspectjSignature =
            (org.aspectj.lang.reflect.ConstructorSignature)makeSignature(clazz, constructor);

        if (aspectjSignature == null) {
            return null;
        }

        MethodLocation4 methodLocation = makeMethodLocation(aspectjSignature, makeLocation(clazz));
        return MethodSignature4.builder()
            .aspectjString(stripModifiersAndReturnType(methodLocation.getSignature()))
            .bridge(false)
            .declaringType(aspectjSignature.getDeclaringTypeName())
            .exceptionTypes(classArrayToString(aspectjSignature.getExceptionTypes()))
            .methodName(aspectjSignature.getName())
            .modifiers(Modifier.toString(aspectjSignature.getModifiers()))
            .packageName(aspectjSignature.getDeclaringType().getPackage().getName())
            .parameterTypes(classArrayToString(aspectjSignature.getParameterTypes()))
            .returnType("")
            .synthetic(constructor.isSynthetic())
            .location(methodLocation.getLocation())
            .annotations(buildAnnotations(clazz, constructor.getName(), constructor.getDeclaredAnnotations()))
            .build();
    }

    private static String classArrayToString(Class<?>[] classes) {
        List<String> classNames = new ArrayList<>();
        for (Class<?> clazz : classes) {
            classNames.add(clazz.getName());
        }

        return StringUtils.join(", ", classNames);
    }
}
