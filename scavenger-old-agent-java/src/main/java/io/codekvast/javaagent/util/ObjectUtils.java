package io.codekvast.javaagent.util;

public class ObjectUtils {
    public static <T> T defaultIfNull(T object, T defaultObject) {
        return object != null ? object : defaultObject;
    }
}
