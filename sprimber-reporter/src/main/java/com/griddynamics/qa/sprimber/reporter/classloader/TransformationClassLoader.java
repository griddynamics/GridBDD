package com.griddynamics.qa.sprimber.reporter.classloader;

import com.griddynamics.qa.sprimber.reporter.resolver.ReportByteCodeResolver;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class TransformationClassLoader extends URLClassLoader {

    private final ClassLoader originClassLoader;
    private final List<ReportByteCodeResolver> reportByteCodeResolvers;

    public TransformationClassLoader(ClassLoader originClassLoader,
                                     List<ReportByteCodeResolver> reportByteCodeResolvers) {
        super(new URL[]{}, originClassLoader);
        this.originClassLoader = originClassLoader;
        this.reportByteCodeResolvers = reportByteCodeResolvers;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return reportByteCodeResolvers.stream()
                .filter(resolver -> resolver.isSupportedClass(name))
                .findFirst()
                .map(resolver -> resolver.insertReport(name, this, originClassLoader))
                .orElse(super.loadClass(name));
    }

}
