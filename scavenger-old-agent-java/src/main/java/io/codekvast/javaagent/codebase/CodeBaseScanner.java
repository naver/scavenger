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
package io.codekvast.javaagent.codebase;

import static io.codekvast.javaagent.util.ObjectUtils.defaultIfNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import com.google.common.reflect.ClassPath;
import io.codekvast.javaagent.model.v4.MethodSignature4;
import io.codekvast.javaagent.util.SignatureUtils;
import lombok.Builder;
import lombok.Value;
import lombok.extern.java.Log;

/**
 * Analyzes a code base and detects methods to be tracked. It uses Guava ClassPath for retrieving
 * method signature data.
 * modified by NAVER: add annotation filtering option
 *
 * @author olle.hallin@crisp.se
 * @author NAVER
 */
@Log
public class CodeBaseScanner {
    private final Level finest = getFinestLevel();

    private Level getFinestLevel() {
        String name = defaultIfNull(System.getenv("SCAVENGER_FINEST_LEVEL"), System.getenv("CODEKVAST_FINEST_LEVEL"));
        return Level.parse(name == null ? "FINEST" : name.toUpperCase());
    }

    /**
     * Scans the code base for public methods in the correct packages. The result is stored in the
     * code base.
     *
     * @param codeBase The code base to scan.
     * @return The number of classes containing at least one included method.
     */
    public int scanSignatures(CodeBase codeBase) {
        long startedAt = System.currentTimeMillis();
        log.log(finest, "Scanning " + codeBase);

        Set<String> scanned = new HashSet<>();

        try (ScanResult scanResult = scanCodeBase(codeBase)) {
            for (ClassPath.ClassInfo classInfo : scanResult.getClassInfos()) {
                if (scanned.add(classInfo.getResourceName())) {
                    try {
                        Class<?> clazz = classInfo.load();
                        findConstructors(codeBase, clazz);
                        findMethods(codeBase, clazz, codeBase.getConfig().getSeparatedPackages());
                    } catch (Throwable t) {
                        if (classInfo.getPackageName().contains(".WEB-INF.classes.")) {
                            log.log(finest, "Ignoring " + classInfo);
                        } else {
                            log.log(Level.WARNING, "Cannot analyze " + classInfo, t);
                        }
                    }
                } else {
                    log.log(finest, "Ignoring duplicate " + classInfo);
                }
            }
        }

        if (codeBase.isEmpty()) {
            log.warning(
                String.format(
                    "%s does not contain any classes within packages %s.'",
                    codeBase, codeBase.getConfig().getSeparatedPackages()));
        }

        int result = scanned.size();

        log.info(
            String.format(
                "Scanned %s with package prefix %s in %d ms, found %d methods in %d classes.",
                codeBase.getFingerprint(),
                codeBase.getConfig().getSeparatedPackages(),
                System.currentTimeMillis() - startedAt,
                codeBase.size(),
                result));

        return result;
    }

    private ScanResult scanCodeBase(CodeBase codeBase) {
        File explodedDir = null;
        URLClassLoader classLoader;
        JarFile springBootExecutableJar = getSpringBootExecutableJar(codeBase);
        if (springBootExecutableJar != null) {
            explodedDir = com.google.common.io.Files.createTempDir();
            URL[] urls = explodeSpringBootExecutableJar(springBootExecutableJar, explodedDir);
            classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        } else {
            classLoader = new URLClassLoader(codeBase.getUrls(), ClassLoader.getSystemClassLoader());
        }

        return ScanResult.builder()
            .explodedDir(explodedDir)
            .classInfos(getRecognizedClasses(classLoader, codeBase.getConfig().getSeparatedPackages()))
            .build();
    }

    private JarFile getSpringBootExecutableJar(CodeBase codeBase) {
        if (codeBase.getUrls().length != 1) {
            return null;
        }
        URL url = codeBase.getUrls()[0];
        try {
            JarFile jarFile = new JarFile(url.getFile());
            Attributes attributes = jarFile.getManifest().getMainAttributes();
            String mainClass = attributes.getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass != null && mainClass.contains("org.springframework.boot.loader.")) {
                return jarFile;
            }
        } catch (IOException e) {
            log.log(finest, "Cannot analyze " + url);
        }
        return null;
    }

    private URL[] explodeSpringBootExecutableJar(JarFile jarFile, File destDir) {
        long startedAt = System.currentTimeMillis();
        List<URL> result = new ArrayList<>();

        try {
            Attributes attributes = jarFile.getManifest().getMainAttributes();
            String classesDir = attributes.getValue("Spring-Boot-Classes");
            if (classesDir == null) {
                // Later versions of Spring Boot lacks this attribute
                classesDir = "BOOT-INF/classes/";
            }
            String libDir = attributes.getValue("Spring-Boot-Lib");
            if (libDir == null) {
                // Later versions of Spring Boot lacks this attribute
                libDir = "BOOT-INF/lib/";
            }
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                String name = jarEntry.getName();
                File destFile = new File(destDir + File.separator + name);
                if (name.equals(classesDir)) {
                    //
                    // Just adding destFile.toURI().toURL() does not work, since the trailing '/' will
                    // be stripped.
                    //
                    // The java.net.URLClassLoader distinguishes a directory containing classes from a jar by
                    // the trailing slash.
                    //
                    String uri = "file:" + destFile + File.separator;
                    result.add(new URL(uri));
                }

                if (name.startsWith(libDir) && name.endsWith(".jar")) {
                    result.add(destFile.toURI().toURL());
                }

                if (!name.startsWith("BOOT-INF/")) {
                    // Ignore spring boot loader itself
                    continue;
                }

                if (jarEntry.isDirectory()) {
                    destFile.mkdir();
                } else {
                    copy(jarFile.getInputStream(jarEntry), destFile);
                }
            }
            jarFile.close();
        } catch (IOException e) {
            log.severe("Cannot explode " + jarFile + ": " + e);
        }

        long elapsed = System.currentTimeMillis() - startedAt;
        log.info("Exploded Spring Boot executable jar in " + elapsed + " ms");

        return result.toArray(new URL[0]);
    }

    private void copy(InputStream inputStream, File toFile) throws IOException {
        try (InputStream is = new BufferedInputStream(inputStream);
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(toFile.toPath()))) {

            byte[] buffer = new byte[1000];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        }
    }

    private Set<ClassPath.ClassInfo> getRecognizedClasses(
        ClassLoader classLoader, List<String> packages) {
        Set<ClassPath.ClassInfo> result = new HashSet<>();
        try {
            ClassPath classPath = ClassPath.from(classLoader);
            for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
                boolean included = false;
                String packageName = classInfo.getPackageName();
                for (String aPackage : packages) {
                    if (packageName.startsWith(aPackage)) {
                        result.add(classInfo);
                        log.log(finest, "Included " + classInfo.getName());
                        included = true;
                    }
                }
                if (!included) {
                    log.log(finest, "Ignored " + classInfo.getName());
                }
            }
        } catch (IOException e) {
            log.severe("Cannot create ClassPath: " + e);
        }
        return result;
    }

    void findConstructors(CodeBase codeBase, Class<?> clazz) {
        log.log(finest, "Analyzing " + clazz);
        try {
            Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();

            for (Constructor<?> constructor : declaredConstructors) {
                MethodSignature4 thisSignature =
                    SignatureUtils.makeConstructorSignature(clazz, constructor);
                codeBase.addSignature(thisSignature);
            }

            for (Class<?> innerClass : clazz.getDeclaredClasses()) {
                findConstructors(codeBase, innerClass);
            }
        } catch (NoClassDefFoundError e) {
            log.log(Level.WARNING, "Cannot analyze " + clazz, e);
        }
    }

    void findMethods(CodeBase codeBase, Class<?> clazz, List<String> packages) {
        log.log(finest, "Analyzing " + clazz);
        try {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                boolean ignored = true;
                MethodSignature4 signature = SignatureUtils.makeMethodSignature(clazz, method);

                if (signature == null) {
                    log.log(finest, "Ignoring method " + method + ", cannot make AspectJ signature");
                } else {
                    String declaringPackage = method.getDeclaringClass().getPackage().getName();
                    for (String pkg : packages) {
                        if (declaringPackage.equals(pkg) || declaringPackage.startsWith(pkg + ".")) {
                            codeBase.addSignature(signature);
                            ignored = false;
                        }
                    }
                }
                if (ignored) {
                    log.log(finest, "Ignored " + signature);
                }
            }

            for (Class<?> innerClass : clazz.getDeclaredClasses()) {
                findMethods(codeBase, innerClass, packages);
            }
        } catch (NoClassDefFoundError e) {
            log.log(Level.WARNING, "Cannot analyze " + clazz, e);
        }
    }

    @Value
    @Builder
    static class ScanResult implements AutoCloseable {
        Set<ClassPath.ClassInfo> classInfos;
        File explodedDir;

        @Override
        public void close() {
            classInfos.clear();
            delete(explodedDir);
        }

        private void delete(File file) {
            if (file != null) {
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        delete(f);
                    }
                }
                file.delete();
            }
        }
    }
}
