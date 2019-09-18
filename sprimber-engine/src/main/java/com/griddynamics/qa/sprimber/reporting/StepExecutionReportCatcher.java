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

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuite;
import com.griddynamics.qa.sprimber.discovery.support.StepDefinitionsAbstractFactory;
import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.executor.ErrorMapper;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import cucumber.runtime.java.StepDefAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.PASSED;

/**
 * @author fparamonov
 */

@Slf4j
@Aspect
@Component
public class StepExecutionReportCatcher {

    private static SprimberEventPublisher eventPublisher;
    private static ErrorMapper errorMapper;
    private static List<StepDefinitionsAbstractFactory.StepDefinitionResolver> converters;

    @Autowired
    public void setEventPublisher(SprimberEventPublisher eventPublisher) {
        StepExecutionReportCatcher.eventPublisher = eventPublisher;
    }

    @Autowired
    public void setErrorMapper(ErrorMapper errorMapper) {
        StepExecutionReportCatcher.errorMapper = errorMapper;
    }

    @Autowired
    public void setConverters(List<StepDefinitionsAbstractFactory.StepDefinitionResolver> stepDefinitionResolvers) {
        StepExecutionReportCatcher.converters = stepDefinitionResolvers;
    }

    @Around("stepMethodsExecution()")
    public Object surroundStepExecution(ProceedingJoinPoint joinPoint) {
        Object result = null;
        ExecutionResult executionResult = new ExecutionResult(PASSED);
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        StepDefinition stepDefinition = converters.stream()
                .filter(converter -> converter.accept(methodSignature.getMethod()))
                .flatMap(converter -> converter.resolve(methodSignature.getMethod()).stream())
                .findFirst().get();
        TestSuite.Step step = new TestSuite.Step();
        step.setStepDefinition(stepDefinition);
        step.setName(methodSignature.getMethod().getName());
        try {
            log.info("Starting point");
            StepExecutionReportCatcher.eventPublisher.stepStarted(this, step);
            result = joinPoint.proceed();
            step.setExecutionResult(executionResult);
            log.info("Point completed");
        } catch (Throwable throwable) {
            log.error("Some error here");
            executionResult = errorMapper.parseThrowable(throwable);
            executionResult.conditionallyPrintStacktrace();
            step.setExecutionResult(executionResult);
            log.trace(executionResult.getErrorMessage());
            log.error(throwable.getLocalizedMessage());
        } finally {
            StepExecutionReportCatcher.eventPublisher.stepFinished(this, step);
        }
        return result;
    }

    /**
     * This pointcut designed to catch all Cucumber step definition annotation that has parent
     * meta annotation <code>{@link StepDefAnnotation}</code>
     */
    @Pointcut("execution(@(@cucumber.runtime.java.StepDefAnnotation *) public void *(..))")
    public void cucumberStepExecution() {
    }

    /**
     * This pointcut designed to catch all Cucumber step definition annotation that has parent
     * meta annotation <code>{@link StepDefAnnotation}</code>
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

    @Pointcut("within(@com.griddynamics.qa.sprimber.discovery.annotation.TestController *)")
    public void withinTestControlelr() {
    }

    @Pointcut("withincode(@com.griddynamics.qa.sprimber.discovery.annotation.TestMapping * *(..))")
    public void withinTestMappingMethods() {
    }

    @Pointcut("withinTestMappingMethods() && (cucumberStepCall() || cucumberHookCall())")
    public void stepMethodsExecution() {
    }
}
