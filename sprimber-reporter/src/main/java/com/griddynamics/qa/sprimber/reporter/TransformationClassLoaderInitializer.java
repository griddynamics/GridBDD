package com.griddynamics.qa.sprimber.reporter;

import com.griddynamics.qa.sprimber.reporter.classloader.TransformationClassLoader;
import com.griddynamics.qa.sprimber.reporter.exception.TransformationClassLoaderInitializerException;
import com.griddynamics.qa.sprimber.reporter.resolver.ReportByteCodeResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.griddynamics.qa.sprimber.reporter.holder.ReportDescriptionServiceHolder.loadReportDescriptionServices;
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

@Slf4j
public class TransformationClassLoaderInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private static final AtomicBoolean INITIALIZE_TRANSFORMATION_CLASS_LOADER = new AtomicBoolean(true);

    @Override
    public void initialize(@Nonnull GenericApplicationContext context) {
        if (INITIALIZE_TRANSFORMATION_CLASS_LOADER.getAndSet(false)) {
            try {
                val originClassLoader = context.getClassLoader();
                loadReportDescriptionServices(originClassLoader);
                val reportByteCodeResolvers = loadFactories(ReportByteCodeResolver.class, originClassLoader);
                val transformationClassLoader = new TransformationClassLoader(originClassLoader, reportByteCodeResolvers);
                context.setClassLoader(transformationClassLoader);
                for (ReportByteCodeResolver resolver : reportByteCodeResolvers) {
                    for (String className : resolver.getSupportedClassNames()) {
                        val transformedClass = transformationClassLoader.loadClass(className);
                        log.debug("Class was transformed: " + transformedClass);
                    }
                }
            } catch (Throwable e) {
                rethrow(new TransformationClassLoaderInitializerException(
                        "Unable to initialize 'TransformationClassLoader', cause: " + e.getMessage(), e));
            }
        }
    }

}
