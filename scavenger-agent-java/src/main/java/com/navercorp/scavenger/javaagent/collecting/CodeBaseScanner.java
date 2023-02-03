package com.navercorp.scavenger.javaagent.collecting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.model.CodeBase;
import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.model.Method;
import com.navercorp.scavenger.javaagent.model.Visibility;

@Log
public class CodeBaseScanner {
    private final Config config;
    private final List<String> packagesWithEndingDot;
    private final List<String> packagePaths;
    private final List<String> excludePackagePaths;
    private final List<String> additionalPackagePaths;

    public CodeBaseScanner(Config config) {
        this.config = config;
        this.packagesWithEndingDot = config.getPackagesWithEndingDot();
        this.packagePaths = replaceDotToSlash(packagesWithEndingDot);
        this.excludePackagePaths = replaceDotToSlash(config.getExcludePackagesWithEndingDot());
        this.additionalPackagePaths = replaceDotToSlash(config.getAdditionalPackagesWithEndingDot());
    }

    public CodeBase scan() throws IOException {
        long startedAt = System.currentTimeMillis();

        List<Method> methods = scanMethods();

        CodeBase codeBase = new CodeBase(config, methods);

        log.info(
            "[scavenger] codebase(" + codeBase.getCodeBaseFingerprint() + ") scanned in "
                + (System.currentTimeMillis() - startedAt) + " ms: "
                + methods.size() + " methods"
        );

        return codeBase;
    }

    private List<Method> scanMethods() throws IOException {
        List<Method> methods = new ArrayList<>();
        List<String> codeBases = new ArrayList<>(config.getCodeBase());

        if (codeBases.isEmpty()) {
            String classpath = System.getProperty("java.class.path");
            if (classpath != null) {
                codeBases.addAll(Arrays.asList(classpath.split(File.pathSeparator)));
            } else {
                return Collections.emptyList();
            }
        }
        for (String codeBase : codeBases) {
            File codeBaseFile = new File(codeBase);
            if (codeBaseFile.exists()) {
                methods.addAll(scanMethods(codeBaseFile));
            } else {
                log.warning("[scavenger] codebase file " + codeBaseFile + " does not exist");
            }
        }

        if (config.isDebugMode()) {
            for (Method method : methods) {
                log.info("[scavenger] " + method.getSignature() + " is scanned");
            }
        }

        return methods;
    }

    private List<ClassNode> scanClasses(File file) throws IOException {
        if (file.isDirectory()) {
            List<ClassNode> classes = new ArrayList<>();
            for (File innerFile : Objects.requireNonNull(file.listFiles())) {
                classes.addAll(scanClasses(innerFile));
            }
            return classes;
        } else if (file.getName().endsWith(".class") && containsPackageNameInPath(file.getCanonicalPath(), File.separator)) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            if (isMagicValid(bytes)) {
                ClassNode node = getNode(bytes);
                if (filterClass(node)) {
                    return Collections.singletonList(node);
                }
            }
            return Collections.emptyList();
        } else if (file.getName().endsWith(".jar") || file.getName().endsWith(".war")) {
            try (JarInputStream jis = new JarInputStream(Files.newInputStream(file.toPath()))) {
                return scanClasses(jis);
            } catch (IOException e) {
                log.log(Level.SEVERE, "[scavenger] error while loading " + file.getName(), e);
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    private List<ClassNode> scanClasses(JarInputStream jarInputStream) throws IOException {
        List<ClassNode> classes = new ArrayList<>();
        JarEntry innerEntry;

        while ((innerEntry = jarInputStream.getNextJarEntry()) != null) {
            if (!innerEntry.isDirectory()) {
                if (innerEntry.getName().endsWith(".class") && containsPackageNameInPath(innerEntry.getName(), "/")) {
                    byte[] bytes = readAllBytes(jarInputStream);
                    if (isMagicValid(bytes)) {
                        ClassNode node = getNode(bytes);
                        if (filterClass(node)) {
                            classes.add(node);
                        }
                    }
                } else if (innerEntry.getName().endsWith(".jar") || innerEntry.getName().endsWith(".war")) {
                    // When this nested stream(innerStream) closes, the parent stream(jarInputStream) also get closed.
                    // Therefore, innerStream should not be closed here. See FilterInputStream.close(), paas/scavenger#220
                    JarInputStream innerStream = new JarInputStream(jarInputStream);
                    classes.addAll(scanClasses(innerStream));
                }
            }
        }

        return classes;
    }

    private boolean containsPackageNameInPath(String path, String separator) {
        int lastIndexOfSeparator = path.lastIndexOf(separator);
        if (lastIndexOfSeparator == -1) {
            return false;
        }

        String location = path.substring(0, lastIndexOfSeparator).replace(separator, ".") + ".";
        return packagesWithEndingDot.stream().anyMatch(location::contains);
    }

    private boolean filterClass(ClassNode clazz) {
        return clazz != null
            && !isInterface(clazz)
            && isIncludedByPackage(clazz)
            && (isIncludedByAnnotation(clazz) || isAdditionalPackage(clazz));
    }

    private boolean isIncludedByPackage(ClassNode clazz) {
        boolean includedByPackage = packagePaths.stream().anyMatch(clazz.name::startsWith);
        if (includedByPackage) {
            boolean excludedByPackage = excludePackagePaths.stream().anyMatch(clazz.name::startsWith);
            return !excludedByPackage;
        } else {
            return false;
        }
    }

    private boolean isInterface(ClassNode clazz) {
        return (clazz.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
    }

    private boolean isAdditionalPackage(ClassNode clazz) {
        return additionalPackagePaths.stream().anyMatch(clazz.name::startsWith);
    }

    // filter applies to only class not method
    private boolean isIncludedByAnnotation(ClassNode clazz) {
        if (config.getAnnotations().isEmpty()) {
            return true;
        }

        if (clazz.visibleAnnotations == null && clazz.invisibleAnnotations == null) {
            return false;
        }

        List<AnnotationNode> visibleAnnotations;
        if (clazz.visibleAnnotations != null) {
            visibleAnnotations = clazz.visibleAnnotations;
        } else {
            visibleAnnotations = new ArrayList<>();
        }

        List<AnnotationNode> invisibleAnnotations;
        if (clazz.invisibleAnnotations != null) {
            invisibleAnnotations = clazz.invisibleAnnotations;
        } else {
            invisibleAnnotations = new ArrayList<>();
        }

        List<AnnotationNode> annotations = new ArrayList<>();
        annotations.addAll(visibleAnnotations);
        annotations.addAll(invisibleAnnotations);

        List<String> normalizeAnnotations = annotations.stream()
            .map(it -> it.desc)
            .filter(it -> it.startsWith("L"))
            .map(it -> it.substring(1, it.length() - 1).replace("/", "."))
            .collect(Collectors.toList());
        return normalizeAnnotations.stream().anyMatch(config.getAnnotations()::contains);
    }

    private List<Method> scanMethods(File jarFile) throws IOException {
        return scanClasses(jarFile).stream()
            .flatMap(it -> Method.from(it, config.isLegacyCompatibilityMode()))
            .filter(this::filterMethod)
            .collect(Collectors.toList());
    }

    private boolean filterMethod(Method method) {
        return !isExcludedByConstructor(method)
            && !method.isAbstract()
            && !isExcludedByGetterSetter(method)
            && !isExcludedByVisibility(method)
            && !isExcludedSinceTrivial(method);
    }

    private boolean isExcludedByVisibility(Method method) {
        boolean isPublic = method.getVisibility() == Visibility.PUBLIC;
        boolean isProtected = method.getVisibility() == Visibility.PROTECTED;
        boolean isPackagePrivate = method.getVisibility() == Visibility.PACKAGE_PRIVATE;

        switch (config.getMethodVisibility()) {
            case PUBLIC:
                // public을 트래킹하는데 public이 아니면 excluded
                return !isPublic;
            case PROTECTED:
                // protected인 경우 public, protected인 메서드 추적
                return !(isPublic || isProtected);
            case PACKAGE_PRIVATE:
                // package인 경우 public, protected, package인 메서드 추적
                return !(isPublic || isProtected || isPackagePrivate);
            default: // case PRIVATE
                // private 메서드를 추적하는 경우 exclude 안 함
                return false;
        }
    }

    private boolean isExcludedByGetterSetter(Method method) {
        return config.isExcludeGetterSetter() && method.isGetterSetter();
    }

    private boolean isExcludedByConstructor(Method method) {
        return config.isExcludeConstructors() && method.isConstructor();
    }

    private boolean isExcludedSinceTrivial(Method method) {
        return method.getName().equals("toString") && method.getParameterTypes().isEmpty();
    }

    private static ClassNode getNode(byte[] bytes) {
        if (!isMagicValid(bytes)) {
            return null;
        }

        ClassNode cn = new ClassNode();
        new ClassReader(bytes).accept(cn, ClassReader.EXPAND_FRAMES);
        return cn;
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int readBytes;
        byte[] data = new byte[1 << 14];
        while ((readBytes = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, readBytes);
        }

        return buffer.toByteArray();
    }

    private static boolean isMagicValid(byte[] bytes) {
        return bytes.length > 3
            && bytes[0] == (byte)0xca
            && bytes[1] == (byte)0xfe
            && bytes[2] == (byte)0xba
            && bytes[3] == (byte)0xbe;
    }

    private List<String> replaceDotToSlash(List<String> packageNames) {
        return packageNames.stream()
            .map(it -> it.replace(".", "/"))
            .collect(Collectors.toList());
    }
}
