package com.griddynamics.qa.sprimber.reporter.resolver;

import java.util.List;

public interface ReportByteCodeResolver {

    List<String> getSupportedClassNames();

    boolean isSupportedClass(String className);

    Class insertReport(String className, ClassLoader transformationClassLoader, ClassLoader originClassLoader);

}
