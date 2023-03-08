package com.navercorp.scavenger.javaagent.model;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.annotations.VisibleForTesting;
import lombok.Value;

import com.navercorp.scavenger.model.CodeBasePublication;
import com.navercorp.scavenger.util.HashGenerator;

@Value
public class Method {
    String name;

    Visibility visibility;
    String modifiers;
    String declaringType;
    String signature;

    String parameterTypes;

    boolean isAbstract;
    boolean isGetterSetter;

    private Method(ClassNode classNode, MethodNode methodNode, boolean legacyCompatibilityMode) {
        this.name = methodNode.name;
        this.visibility = Visibility.from(methodNode.access);
        this.modifiers = Modifier.toString(methodNode.access);
        this.isAbstract = (methodNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
        this.isGetterSetter = isGetter(methodNode) || isSetter(methodNode);

        if (legacyCompatibilityMode) {
            this.declaringType = classNode.name.replace("/", ".").replace('$', '.' );
        } else {
            this.declaringType = classNode.name.replace("/", ".");
        }
        this.parameterTypes =
            Arrays.stream(Type.getArgumentTypes(methodNode.desc))
                .map(Type::getClassName)
                .collect(Collectors.joining(legacyCompatibilityMode ? ", " : ","));

        this.signature = declaringType + (isConstructor() ? "" : "." + name) +
            "(" + parameterTypes + ")";
    }

    Method(String name, Visibility visibility, String modifiers, boolean isAbstract, boolean isGetterSetter, String declaringType,
        String parameterTypes, String signature) {
        this.name = name;
        this.visibility = visibility;
        this.modifiers = modifiers;
        this.isAbstract = isAbstract;
        this.isGetterSetter = isGetterSetter;
        this.declaringType = declaringType;
        this.parameterTypes = parameterTypes;
        this.signature = signature;
    }

    private boolean isGetter(MethodNode methodNode) {
        Type returnType = Type.getReturnType(methodNode.desc);

        return (methodNode.parameters == null || methodNode.parameters.isEmpty())
            && returnType != Type.VOID_TYPE && (methodNode.name.startsWith("get")
            || (methodNode.name.startsWith("is") && returnType == Type.BOOLEAN_TYPE)
            || (methodNode.name.matches("component\\d+")) // kotlin component
        );
    }

    private boolean isSetter(MethodNode methodNode) {
        return methodNode.name.startsWith("set")
            && methodNode.parameters != null
            && methodNode.parameters.size() == 1
            && Type.getReturnType(methodNode.desc) == Type.VOID_TYPE;
    }

    public boolean isConstructor() {
        return name.equals("<init>");
    }

    public static Stream<Method> from(ClassNode classNode, boolean legacyCompatibilityMode) {
        return classNode.methods.stream()
            .map(it -> new Method(classNode, it, legacyCompatibilityMode));
    }

    @VisibleForTesting
    public static Method createTestMethod() {
        return new Method("hello", Visibility.PRIVATE, "", false, false, "Wow", "", "hello()");
    }

    public CodeBasePublication.CodeBaseEntry toCodeBaseEntry() {

        String packageName = "";
        int lastDotIndex = defaultIfNull(this.declaringType, "").lastIndexOf(".");
        if (lastDotIndex != -1) {
            packageName = this.declaringType.substring(0, lastDotIndex);
        }

        String signature = defaultIfNull(this.signature, "");

        return CodeBasePublication.CodeBaseEntry.newBuilder()
            .setDeclaringType(defaultIfNull(this.declaringType, ""))
            .setVisibility(defaultIfNull(this.visibility, Visibility.PUBLIC).toString())
            .setSignature(signature)
            .setMethodName(defaultIfNull(this.name, ""))
            .setModifiers(defaultIfNull(this.modifiers, ""))
            .setPackageName(defaultIfNull(packageName, ""))
            .setParameterTypes(defaultIfNull(this.parameterTypes, ""))
            .setSignatureHash(HashGenerator.Md5.from(signature))
            .build();
    }

    public <T> T defaultIfNull(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
