package com.griddynamics.qa.sprimber.reporter.utils;

import javassist.CtClass;
import lombok.*;

import java.io.DataInputStream;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ClassUtils {

    private static final char CLASS_NAME_SEPARATOR = '.';
    private static final char CLASS_PATH_SEPARATOR = '/';
    private static final String CLASS_SUFFIX = ".class";

    @SneakyThrows
    public static byte[] classToBytes(String className, ClassLoader classLoader) {
        val classAsPath = className.replace(CLASS_NAME_SEPARATOR, CLASS_PATH_SEPARATOR) + CLASS_SUFFIX;
        val inputStream = classLoader.getResourceAsStream(classAsPath);
        requireNonNull(inputStream, "Class path not found: " + className);
        @Cleanup val dataInputStream = new DataInputStream(inputStream);
        val classBytes = new byte[inputStream.available()];
        dataInputStream.readFully(classBytes);
        return classBytes;
    }

    @SneakyThrows
    public static void deFrostClass(CtClass ctClass) {
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }
        var parent = ctClass.getSuperclass();
        while (parent != null) {
            if (!parent.getName().equals(Object.class.getName()) && parent.isFrozen()) {
                parent.defrost();
            }
            parent = parent.getSuperclass();
        }
    }

}
