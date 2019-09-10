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
import com.griddynamics.qa.sprimber.discovery.support.StepDefinitionsDiscovery;
import com.griddynamics.qa.sprimber.engine.executor.ErrorMapper;
import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
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

import static com.griddynamics.qa.sprimber.engine.model.ExecutionResult.Status.PASSED;

/**
 * @author fparamonov
 */

@Slf4j
@Aspect
@Component
public class StepExecutionReportCatcher {

    private static SprimberEventPublisher eventPublisher;
    private static ErrorMapper errorMapper;
    private static List<StepDefinitionsDiscovery.StepDefinitionConverter> converters;

    @Autowired
    public void setEventPublisher(SprimberEventPublisher eventPublisher) {
        StepExecutionReportCatcher.eventPublisher = eventPublisher;
    }

    @Autowired
    public void setErrorMapper(ErrorMapper errorMapper) {
        StepExecutionReportCatcher.errorMapper = errorMapper;
    }

    @Autowired
    public void setConverters(List<StepDefinitionsDiscovery.StepDefinitionConverter> stepDefinitionConverters) {
        StepExecutionReportCatcher.converters = stepDefinitionConverters;
    }

    @Around("stepMethodsExecution()")
    public Object surroundStepExecution(ProceedingJoinPoint joinPoint) {
        Object result = null;
        ExecutionResult executionResult = new ExecutionResult(PASSED);
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        StepDefinition stepDefinition = converters.stream()
                .flatMap(converter -> converter.convert(methodSignature.getMethod()).stream())
                .findAny().get();
        try {
            log.info("Starting point");
            StepExecutionReportCatcher.eventPublisher.stepStarted(this, stepDefinition);
            result = joinPoint.proceed();
            stepDefinition.setExecutionResult(executionResult);
            log.info("Point completed");
        } catch (Throwable throwable) {
            log.error("Some error here");
            executionResult = errorMapper.parseThrowable(throwable);
            executionResult.conditionallyPrintStacktrace();
            stepDefinition.setExecutionResult(executionResult);
            log.trace(executionResult.getErrorMessage());
            log.error(throwable.getLocalizedMessage());
        } finally {
            StepExecutionReportCatcher.eventPublisher.stepFinished(this, stepDefinition);
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
     * This pointcut designed to catch the Cucumber hook annotations
     */
    @Pointcut("execution(@cucumber.api.java.* public void *(..))")
    public void cucumberHookExecution() {
    }

    @Pointcut("within(@com.griddynamics.qa.sprimber.engine.model.action.Actions *)")
    public void withinTest() {
    }

    @Pointcut("withinTest() && (cucumberStepExecution() || cucumberHookExecution())")
    public void stepMethodsExecution() {
    }
}
