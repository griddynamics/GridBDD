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

package com.griddynamics.qa.sprimber.aspect;

import com.griddynamics.qa.sprimber.discovery.step.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.step.support.StepDefinitionsDiscovery;
import com.griddynamics.qa.sprimber.engine.executor.ErrorMapper;
import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import cucumber.runtime.java.StepDefAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.griddynamics.qa.sprimber.engine.model.ExecutionResult.Status.PASSED;

/**
 * @author fparamonov
 */

@Slf4j
@Aspect
@Component
public class StepExecutionCatcher {

    private static ApplicationEventPublisher eventPublisher;
    private static ErrorMapper errorMapper;
    private static List<StepDefinitionsDiscovery.StepDefinitionConverter> converters;

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        StepExecutionCatcher.eventPublisher = eventPublisher;
    }

    @Autowired
    public void setErrorMapper(ErrorMapper errorMapper) {
        StepExecutionCatcher.errorMapper = errorMapper;
    }

    @Autowired
    public void setConverters(List<StepDefinitionsDiscovery.StepDefinitionConverter> stepDefinitionConverters) {
        StepExecutionCatcher.converters = stepDefinitionConverters;
    }

    @Around("stepMethodsExecution()")
    public Object surroundStepExecution(ProceedingJoinPoint joinPoint) {
        Object result = null;
        ExecutionResult executionResult = new ExecutionResult(PASSED);
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        StepDefinition stepDefinition = converters.stream()
                .flatMap(converter -> converter.convert(methodSignature.getMethod()).stream())
                .findAny().get();
        StepStartedEvent stepStartedEvent = new StepStartedEvent(joinPoint.getTarget(), stepDefinition);
        StepFinishedEvent stepFinishedEvent = new StepFinishedEvent(joinPoint.getTarget(), stepDefinition);
        try {
            log.info("Starting point");
            StepExecutionCatcher.eventPublisher.publishEvent(stepStartedEvent);
            result = joinPoint.proceed();
            stepFinishedEvent.setExecutionResult(executionResult);
            StepExecutionCatcher.eventPublisher.publishEvent(stepFinishedEvent);
            log.info("Point completed");
        } catch (Throwable throwable) {
            log.error("Some error here");
            executionResult = errorMapper.parseThrowable(throwable);
            executionResult.conditionallyPrintStacktrace();
            log.trace(executionResult.getErrorMessage());
            log.error(throwable.getLocalizedMessage());
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
