/*
Copyright (c) 2010-2018 Grid Dynamics International, Inc. All Rights Reserved
http://www.griddynamics.com

This library is free software; you can redistribute it and/or modify it under the terms of
the GNU Lesser General Public License as published by the Free Software Foundation; either
version 2.1 of the License, or any later version.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

$Id: 
@Project:     Sprimber
@Description: Framework that provide bdd engine and bridges for most popular BDD frameworks
*/

package com.griddynamics.qa.sprimber.reporting;

import com.griddynamics.qa.sprimber.engine.NodeFallbackManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;

/**
 * @author fparamonov
 */

@Slf4j
@Aspect
public class StepExecutionReportCatcher {

    private static SprimberEventPublisher eventPublisher;
    private static NodeFallbackManager nodeFallbackManager;
//    private static SpringTestMethodsLoader stepDefinitionsFactory;

    @Autowired
    public void setEventPublisher(SprimberEventPublisher eventPublisher) {
        StepExecutionReportCatcher.eventPublisher = eventPublisher;
    }

    @Autowired
    public void setErrorMapper(NodeFallbackManager nodeFallbackManager) {
        StepExecutionReportCatcher.nodeFallbackManager = nodeFallbackManager;
    }

//    @Autowired
//    public void setStepDefinitionsFactory(SpringTestMethodsLoader stepDefinitionsFactory) {
//        StepExecutionReportCatcher.stepDefinitionsFactory = stepDefinitionsFactory;
//    }

    @Around("stepMethodsExecution()")
    public Object surroundStepExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        result = joinPoint.proceed();
//        ExecutionResult executionResult = new ExecutionResult(PASSED);
//        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//        StepDefinition stepDefinition = stepDefinitionsFactory.load().get(buildHash(methodSignature.getMethod()));
//        TestSuite.Step step = new TestSuite.Step();
//        step.setStepDefinition(stepDefinition);
//        step.setName(stepDefinition.getName());
//        try {
//            log.info("Starting point");
//            StepExecutionReportCatcher.eventPublisher.utilityStepStarted(this, step);
//            result = joinPoint.proceed();
//            step.setExecutionResult(executionResult);
//            log.info("Point completed");
//        } catch (Throwable throwable) {
//            log.error("Some error here");
//            executionResult = nodeFallbackManager.parseThrowable(throwable);
//            executionResult.conditionallyPrintStacktrace();
//            step.setExecutionResult(executionResult);
//            log.trace(executionResult.getErrorMessage());
//            log.error(throwable.getLocalizedMessage());
//        } finally {
//            StepExecutionReportCatcher.eventPublisher.utilityStepFinished(this, step);
//        }
        return result;
    }

    /**
     * This pointcut designed to catch all Cucumber step definition annotation that has parent
     * meta annotation <code>{@link cucumber.runtime.java.StepDefAnnotation}</code>
     */
    @Pointcut("execution(@(@cucumber.runtime.java.StepDefAnnotation *) public void *(..))")
    public void cucumberStepExecution() {
    }

    /**
     * This pointcut designed to catch all Cucumber step definition annotation that has parent
     * meta annotation <code>{@link cucumber.runtime.java.StepDefAnnotation}</code>
     */
    @Pointcut("call(@(@cucumber.runtime.java.StepDefAnnotation *) public void *(..))")
    public void cucumberStepCall() {
    }

    /**
     * This pointcut designed to catch the Cucumber hook annotations
     */
    @Pointcut("execution(@cucumber.api.java.* public void *(..))")
    public void cucumberHookExecution() {
    }

    /**
     * This pointcut designed to catch the Cucumber hook annotations
     */
    @Pointcut("call(@cucumber.api.java.* public void *(..))")
    public void cucumberHookCall() {
    }

    @Pointcut("within(@com.griddynamics.qa.sprimber.discovery.TestController *)")
    public void withinTestControlelr() {
    }

    @Pointcut("withincode(@com.griddynamics.qa.sprimber.discovery.TestMapping * *(..))")
    public void withinTestMappingMethods() {
    }

    @Pointcut("withinTestMappingMethods() && (cucumberStepCall() || cucumberHookCall())")
    public void stepMethodsExecution() {
    }

    private String buildHash(Method method) {
        String uniqueName = method.getDeclaringClass().getCanonicalName() + "#" +
                method.getName() + "#" +
                method.getParameterCount();
        return DigestUtils.md5DigestAsHex(uniqueName.getBytes());
    }
}
