package com.griddynamics.qa.sprimber.reporter.resolver;

import com.griddynamics.qa.sprimber.reporter.exception.ReportByteCodeResolverException;
import com.griddynamics.qa.sprimber.reporter.model.MethodInfo;
import com.griddynamics.qa.sprimber.reporter.model.TestStatusType;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import lombok.SneakyThrows;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.griddynamics.qa.sprimber.reporter.model.TestStatusType.FAILED;
import static com.griddynamics.qa.sprimber.reporter.model.TestStatusType.SUCCESS;
import static com.griddynamics.qa.sprimber.reporter.template.ReportMethodTemplates.PROCESS_REPORT_CODE_BLOCK_MASK;
import static com.griddynamics.qa.sprimber.reporter.template.ReportMethodTemplates.ASSERTJ_SEND_REPORT_CODE_BLOCK_MASK;
import static com.griddynamics.qa.sprimber.reporter.utils.ClassUtils.classToBytes;
import static com.griddynamics.qa.sprimber.reporter.utils.ClassUtils.deFrostClass;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static javassist.ClassPool.getDefault;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public abstract class CommonReportByteCodeResolver implements ReportByteCodeResolver {

    private static final Map<String, Class<?>> CACHED_TRANSFORMED_CLASSES = new ConcurrentHashMap<>();

    private static final String TRANSFORMED_METHOD_NAME_PREFIX = "sr$";
    private static final String METHOD_PARAM_PREFIX = "$";
    private static final String METHDO_PARAM_DELIMITER = ",";
    private static final String METHOD_OPEN_BRACKET = "(";
    private static final String METHOD_CLOSED_BRACKET = ");";

    private static final String WRAPPED_STRING_QUOTE = "\"";
    private static final String UUID_DELIMITER = "-";

    protected Class<?> acceptMethodsTransformation(String className,
                                                   ClassLoader transformationClassLoader, ClassLoader originClassLoader,
                                                   Consumer<Stream<MethodInfo>> methodsInfoConsumer) {
        if (!CACHED_TRANSFORMED_CLASSES.containsKey(className)) {
            synchronized (CommonReportByteCodeResolver.class) {
                if (!CACHED_TRANSFORMED_CLASSES.containsKey(className)) {
                    try {
                        doAcceptMethodsTransformation(className,
                                transformationClassLoader, originClassLoader, methodsInfoConsumer);
                    } catch (Throwable e) {
                        return rethrow(new ReportByteCodeResolverException(
                                "Unable to transform methods, class: " + className, e));
                    }
                }
            }
        }
        return CACHED_TRANSFORMED_CLASSES.get(className);
    }

    protected void injectReportToMethod(MethodInfo methodInfo) {
        val assertionDescriptionParamIndex = findAssertionDescriptionParamIndex(methodInfo);
        if (assertionDescriptionParamIndex != -1) {
            setupReportProcessingToByteCode(methodInfo.getCtClass(),
                    methodInfo.getCtMethod(), assertionDescriptionParamIndex);
        }
    }

    protected boolean isAlreadyTransformedMethod(CtMethod ctMethod) {
        return ctMethod.getName().startsWith(TRANSFORMED_METHOD_NAME_PREFIX);
    }

    protected abstract int findAssertionDescriptionParamIndex(MethodInfo methodInfo);

    @SneakyThrows
    private void doAcceptMethodsTransformation(String className,
                                               ClassLoader transformationClassLoader, ClassLoader originClassLoader,
                                               Consumer<Stream<MethodInfo>> methodsInfoConsumer) {
        val classPool = getDefault();
        classPool.appendClassPath(new LoaderClassPath(transformationClassLoader));
        val ctClass = classPool.makeClass(new ByteArrayInputStream(classToBytes(className, originClassLoader)));
        val methodsInfoStream = stream(ctClass.getMethods())
                .map(ctMethod -> new MethodInfo(ctClass, ctMethod));
        methodsInfoConsumer.accept(methodsInfoStream);
        CACHED_TRANSFORMED_CLASSES.put(className, ctClass.toClass());
    }

    @SneakyThrows
    private void setupReportProcessingToByteCode(CtClass ctClass, CtMethod ctMethod,
                                                 int assertionDescriptionParamIndex) {
        val transformedCtMethod = new CtMethod(ctMethod, ctClass, null);
        deFrostClass(ctClass);
        ctMethod.setName(generateMethodName());
        val sendSuccessReportCodeBlock = buildSendReportCodeBlock(ctClass, assertionDescriptionParamIndex, SUCCESS);
        val sendFailedReportCodeBlock = buildSendReportCodeBlock(ctClass, assertionDescriptionParamIndex, FAILED);
        val transformedMethodCodeBlock = format(PROCESS_REPORT_CODE_BLOCK_MASK,
                buildInvokeOriginMethodCodeBlock(ctMethod), sendSuccessReportCodeBlock, sendFailedReportCodeBlock);
        transformedCtMethod.setBody(transformedMethodCodeBlock);
        ctClass.addMethod(transformedCtMethod);
    }

    private String generateMethodName() {
        return TRANSFORMED_METHOD_NAME_PREFIX + randomUUID().toString()
                .replaceAll(UUID_DELIMITER, EMPTY);
    }

    private String buildSendReportCodeBlock(CtClass ctClass, int assertionDescriptionParamIndex,
                                            TestStatusType testStatusType) {
        val status = WRAPPED_STRING_QUOTE + testStatusType + WRAPPED_STRING_QUOTE;
        val className = WRAPPED_STRING_QUOTE + ctClass.getName() + WRAPPED_STRING_QUOTE;
        return format(ASSERTJ_SEND_REPORT_CODE_BLOCK_MASK, className,
                assertionDescriptionParamIndex, assertionDescriptionParamIndex, status);
    }

    @SneakyThrows
    private String buildInvokeOriginMethodCodeBlock(CtMethod ctMethod) {
        val methodParametersCodeBlock = new StringBuilder();
        for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
            methodParametersCodeBlock.append(METHOD_PARAM_PREFIX).append(i + 1);
            if ((i + 1) < ctMethod.getParameterTypes().length) {
                methodParametersCodeBlock.append(METHDO_PARAM_DELIMITER);
            }
        }
        return ctMethod.getName() + METHOD_OPEN_BRACKET + methodParametersCodeBlock.toString() + METHOD_CLOSED_BRACKET;
    }

}
