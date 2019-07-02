package com.griddynamics.qa.sprimber.reporter.resolver.assertj;

import com.google.common.collect.ImmutableList;
import com.griddynamics.qa.sprimber.reporter.exception.ReportByteCodeResolverException;
import com.griddynamics.qa.sprimber.reporter.model.MethodInfo;
import com.griddynamics.qa.sprimber.reporter.resolver.CommonReportByteCodeResolver;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.assertj.core.api.AssertionInfo;

import java.util.List;

import static javassist.bytecode.AccessFlag.isPublic;
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public class AssertJReportByteCodeResolver extends CommonReportByteCodeResolver {

    public static final List<String> ASSERTJ_SUPPORTED_CLASS_NAMES = ImmutableList.of(
            // should be processed first, link sequence are required
            "org.assertj.core.internal.Comparables",

            "org.assertj.core.internal.Arrays",
            "org.assertj.core.internal.BooleanArrays",
            "org.assertj.core.internal.Booleans",
            "org.assertj.core.internal.ByteArrays",
            "org.assertj.core.internal.Characters",
            "org.assertj.core.internal.CharArrays",
            "org.assertj.core.internal.Classes",
            "org.assertj.core.internal.CommonValidations",
            "org.assertj.core.internal.Conditions",
            "org.assertj.core.internal.Dates",
            "org.assertj.core.internal.DoubleArrays",
            "org.assertj.core.internal.Files",
            "org.assertj.core.internal.FloatArrays",
            "org.assertj.core.internal.Futures",
            "org.assertj.core.internal.InputStreams",
            "org.assertj.core.internal.IntArrays",
            "org.assertj.core.internal.Iterables",
            "org.assertj.core.internal.Lists",
            "org.assertj.core.internal.LongArrays",
            "org.assertj.core.internal.Maps",
            "org.assertj.core.internal.Numbers",
            "org.assertj.core.internal.ObjectArrays",
            "org.assertj.core.internal.Objects",
            "org.assertj.core.internal.Paths",
            "org.assertj.core.internal.RealNumbers",
            "org.assertj.core.internal.ShortArrays",
            "org.assertj.core.internal.Strings",
            "org.assertj.core.internal.Throwables",
            "org.assertj.core.internal.Uris",
            "org.assertj.core.internal.Urls"
    );

    @Override
    public List<String> getSupportedClassNames() {
        return ASSERTJ_SUPPORTED_CLASS_NAMES;
    }

    @Override
    public boolean isSupportedClass(String className) {
        return ASSERTJ_SUPPORTED_CLASS_NAMES.contains(className);
    }

    @Override
    public Class insertReport(String className,
                              ClassLoader transformationClassLoader, ClassLoader originClassLoader) {
        try {
            return acceptMethodsTransformation(className
                    , transformationClassLoader, originClassLoader, methodsInfoStream ->
                            methodsInfoStream.forEach(this::injectReportByClassOfMethod));
        } catch (Throwable e) {
            return rethrow(new ReportByteCodeResolverException(
                    "Unable to inject 'report', target class: " + className, e));
        }
    }

    @SneakyThrows
    protected int findAssertionDescriptionParamIndex(MethodInfo methodInfo) {
        val ctMethod = methodInfo.getCtMethod();
        var assertionDescriptionParamIndex = -1;
        if (isPublic(ctMethod.getModifiers())) {
            for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
                val paramType = ctMethod.getParameterTypes()[i];
                if (AssertionInfo.class.getName().equals(paramType.getName())) {
                    assertionDescriptionParamIndex = i + 1;
                    break;
                }
            }
        }
        return assertionDescriptionParamIndex;
    }

    private void injectReportByClassOfMethod(MethodInfo methodInfo) {
        val ctClass = methodInfo.getCtClass();
        val ctMethod = methodInfo.getCtMethod();
        if (!isAlreadyTransformedMethod(ctMethod) && ASSERTJ_SUPPORTED_CLASS_NAMES.contains(ctClass.getName())) {
            injectReportToMethod(methodInfo);
        }
    }

}
