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

package com.griddynamics.qa.sprimber.discovery.support;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.executor.ErrorMapper;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.PASSED;
import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.SKIPPED;

/**
 * @author fparamonov
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BddTestExecutor implements TestSuiteDefinition.TestExecutor {

    private final Executor sprimberTestExecutor;
    private final ErrorMapper errorMapper;
    private final ApplicationContext applicationContext;
    private final SprimberEventPublisher eventPublisher;

    @Override
    public CompletableFuture<ExecutionResult> execute(TestSuiteDefinition.TestDefinition testDefinition) {
        return CompletableFuture.supplyAsync(() -> executeTest(testDefinition), sprimberTestExecutor);
    }

    private ExecutionResult executeTest(TestSuiteDefinition.TestDefinition testDefinition) {
        eventPublisher.testStarted(this, testDefinition);
        testDefinition.setExecutionResult(new ExecutionResult(PASSED));
        List<CompletableFuture<ExecutionResult>> stepResults = testDefinition.getStepDefinitions().stream()
                .map(stepDefinition -> initStepDefinitionStage(stepDefinition)
                        .handle((stepDef, throwable) -> {
                            eventPublisher.stepStarted(this, stepDefinition);
                            if (testDefinition.isFallbackActive()) {
                                if (!(testDefinition.getFallbackStrategy().allowedTypes().contains(stepDef.getStepType()) &&
                                        testDefinition.getFallbackStrategy().allowedPhases().contains(stepDef.getStepPhase()))) {
                                    stepDef.setSkipped(true);
                                }
                            }
                            return stepDef;
                        })
                        .thenApply(this::executeStepDefinition)
                        .exceptionally(throwable -> handleStepException(testDefinition, stepDefinition, throwable))
                        .handle((executionResult, throwable) -> {
                            eventPublisher.stepFinished(this, stepDefinition);
                            if (testDefinition.isFallbackActive()) {
                                testDefinition.getFallbackStrategy().updateScope(stepDefinition);
                            }
                            return executionResult;
                        })
                )
                .collect(Collectors.toList());
        CompletableFuture<Void> done = CompletableFuture.allOf(stepResults.toArray(new CompletableFuture[0]));
        Map<ExecutionResult.Status, Long> statistic = done.thenApply(v -> stepResults.stream()
                .map(CompletionStage::toCompletableFuture)
                .map(CompletableFuture::join)
                .collect(Collectors.groupingBy(ExecutionResult::getStatus, Collectors.counting()))).join();
        testDefinition.getExecutionResult().getStatistic().putAll(statistic);
        eventPublisher.testFinished(this, testDefinition);
        return testDefinition.getExecutionResult();
    }

    private ExecutionResult handleStepException(TestSuiteDefinition.TestDefinition testDefinition, StepDefinition stepDefinition, Throwable throwable) {
        ExecutionResult result = errorMapper.parseThrowable(throwable);
        stepDefinition.setExecutionResult(result);
        result.conditionallyPrintStacktrace();
        testDefinition.setExecutionResult(result);
        testDefinition.activeFallback();
        log.trace(result.getErrorMessage());
        log.error(throwable.getLocalizedMessage());
        return result;
    }

    private ExecutionResult executeStepDefinition(StepDefinition stepDefinition) {
        if (stepDefinition.isSkipped()) {
            stepDefinition.setExecutionResult(new ExecutionResult(SKIPPED));
            return new ExecutionResult(SKIPPED);
        }
        Method testMethod = stepDefinition.getMethod();
        Object target = applicationContext.getBean(testMethod.getDeclaringClass());
        Object[] args = stepDefinition.getParameters().isEmpty() ? new Object[0] : stepDefinition.getParameters().values().toArray();
        ReflectionUtils.invokeMethod(testMethod, target, args);
        return new ExecutionResult(PASSED);
    }

    private CompletableFuture<StepDefinition> initStepDefinitionStage(StepDefinition stepDefinition) {
        stepDefinition.setExecutionResult(new ExecutionResult(PASSED));
        return CompletableFuture.completedFuture(stepDefinition);
    }
}
