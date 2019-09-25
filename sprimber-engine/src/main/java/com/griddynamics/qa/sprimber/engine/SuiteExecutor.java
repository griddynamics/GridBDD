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

package com.griddynamics.qa.sprimber.engine;

import com.griddynamics.qa.sprimber.common.TestSuite;
import com.griddynamics.qa.sprimber.discovery.ExecutionContext;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.PASSED;
import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.SKIPPED;

/**
 * @author fparamonov
 */

@Slf4j
@RequiredArgsConstructor
public class SuiteExecutor {

    private final Executor sprimberTestCaseExecutor;
    private final Executor sprimberTestExecutor;
    private final ErrorMapper errorMapper;
    private final ExecutionContext executionContext;
    private final ApplicationContext applicationContext;
    private final SprimberEventPublisher eventPublisher;

    public ExecutionResult executeTestSuite(TestSuite testSuite) {
        eventPublisher.testSuiteStarted(this, testSuite);
        List<CompletableFuture<ExecutionResult>> testCases = testSuite.getTestCases().stream()
                .map(testCase -> initTestCase(testCase)
                        .thenApply(tc -> executionContext.getHookOnlyStepFactory().provideBeforeTestSuiteHooks().stream()
                                .map(this::executeStep)
                                .collect(new ExecutionResult.ExecutionResultCollector()))
                        .exceptionally(this::handleTestCaseException)
                        .thenApplyAsync(executionResult -> executeTestCase(testCase), sprimberTestCaseExecutor)
                        .exceptionally(this::handleTestCaseException)
                        .thenApply(executionResult -> executionContext.getHookOnlyStepFactory().provideAfterTestSuiteHooks()
                                .stream()
                                .map(this::executeStep)
                                .collect(new ExecutionResult.ExecutionResultCollector()))
                        .exceptionally(this::handleTestCaseException)
                )
                .collect(Collectors.toList());
        CompletableFuture<List<ExecutionResult>> executedTestCases =
                CompletableFuture.allOf(testCases.toArray(new CompletableFuture[0]))
                        .thenApply(v -> testCases.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
                        );
        ExecutionResult suiteResult = executedTestCases.join().stream().collect(new ExecutionResult.ExecutionResultCollector());
        testSuite.setExecutionResult(suiteResult);
        eventPublisher.testSuiteFinished(this, testSuite);
        log.info("Suite completed with status '{}' and test case details '{}'", suiteResult.getStatus(), suiteResult.getStatistic());
        return suiteResult;
    }

    public ExecutionResult executeTestCase(TestSuite.TestCase testCase) {
        eventPublisher.testCaseStarted(this, testCase);
        List<CompletableFuture<ExecutionResult>> tests = testCase.getTests().stream()
                .map(test -> initTest(test)
                        .thenApply(executionResult -> executionContext.getHookOnlyStepFactory().provideBeforeTestCaseHooks().stream()
                                .map(this::executeStep)
                                .collect(new ExecutionResult.ExecutionResultCollector()))
                        .exceptionally(this::handleTestException)
                        .thenApplyAsync(executionResult -> executeTest(test), sprimberTestExecutor)
                        .exceptionally(this::handleTestException)
                        .thenApply(executionResult -> executionContext.getHookOnlyStepFactory().provideAfterTestCaseHooks().stream()
                                .map(this::executeStep)
                                .collect(new ExecutionResult.ExecutionResultCollector(executionResult)))
                        .exceptionally(this::handleTestException)
                )
                .collect(Collectors.toList());
        CompletableFuture<List<ExecutionResult>> executedTests =
                CompletableFuture.allOf(tests.toArray(new CompletableFuture[0]))
                        .thenApply(v -> tests.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
                        );
        ExecutionResult testCaseResult = executedTests.join().stream().collect(new ExecutionResult.ExecutionResultCollector());
        testCase.setExecutionResult(testCaseResult);
        log.info("Test case completed with status '{}' and test details '{}'", testCaseResult.getStatus(), testCaseResult.getStatistic());
        eventPublisher.testCaseFinished(this, testCase);
        return testCaseResult;
    }

    private ExecutionResult executeTest(TestSuite.Test test) {
        eventPublisher.testStarted(this, test);
        List<CompletableFuture<ExecutionResult>> steps = test.getSteps().stream()
                .map(step -> initStep(step)
                        .handle((stepDef, throwable) -> {
                            eventPublisher.stepStarted(this, step);
                            if (test.isFallbackActive()) {
                                if (!(test.getFallbackStrategy().allowedTypes().contains(step.getStepDefinition().getStepType()) &&
                                        test.getFallbackStrategy().allowedPhases().contains(step.getStepDefinition().getStepPhase()))) {
                                    step.setSkipped(true);
                                }
                            }
                            return step;
                        })
                        .thenApply(this::executeStep)
                        .exceptionally(throwable -> handleStepException(test, step, throwable))
                        .handle((executionResult, throwable) -> {
                            eventPublisher.stepFinished(this, step);
                            if (test.isFallbackActive()) {
                                test.getFallbackStrategy().updateScope(step);
                            }
                            return executionResult;
                        })
                )
                .collect(Collectors.toList());
        CompletableFuture<List<ExecutionResult>> executedSteps =
                CompletableFuture.allOf(steps.toArray(new CompletableFuture[0]))
                        .thenApply(v -> steps.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
                        );
        ExecutionResult testResult = executedSteps.join().stream().collect(new ExecutionResult.ExecutionResultCollector());
        test.setExecutionResult(testResult);
        log.info("Test completed with status '{}' and step details '{}'", testResult.getStatus(), testResult.getStatistic());
        eventPublisher.testFinished(this, test);
        return testResult;
    }

    private CompletableFuture<TestSuite.TestCase> initTestCase(TestSuite.TestCase testCase) {
        testCase.setExecutionResult(ExecutionResult.builder().status(PASSED).build());
        return CompletableFuture.completedFuture(testCase);
    }

    private CompletableFuture<TestSuite.Test> initTest(TestSuite.Test test) {
        test.setExecutionResult(ExecutionResult.builder().status(PASSED).build());
        return CompletableFuture.completedFuture(test);
    }

    private CompletableFuture<TestSuite.Step> initStep(TestSuite.Step step) {
        step.setExecutionResult(ExecutionResult.builder().status(PASSED).build());
        return CompletableFuture.completedFuture(step);
    }

    private ExecutionResult handleTestCaseException(Throwable throwable) {
        ExecutionResult result = errorMapper.parseThrowable(throwable);
        result.conditionallyPrintStacktrace();
        return result;
    }

    private ExecutionResult handleTestException(Throwable throwable) {
        ExecutionResult result = errorMapper.parseThrowable(throwable);
        result.conditionallyPrintStacktrace();
        return result;
    }

    private ExecutionResult handleStepException(TestSuite.Test test, TestSuite.Step step, Throwable throwable) {
        ExecutionResult result = errorMapper.parseThrowable(throwable);
        step.setExecutionResult(result);
        result.conditionallyPrintStacktrace();
        test.setExecutionResult(result);
        test.activeFallback();
        log.trace(result.getErrorMessage());
        log.error(throwable.getLocalizedMessage());
        return result;
    }

    private ExecutionResult executeStep(TestSuite.Step step) {
        if (step.isSkipped()) {
            step.setExecutionResult(new ExecutionResult(SKIPPED));
            return new ExecutionResult(SKIPPED);
        }
        Method testMethod = step.getStepDefinition().getMethod();
        Object target = applicationContext.getBean(testMethod.getDeclaringClass());
        Object[] args = step.getParameters().isEmpty() ?
                new Object[0] : step.getParameters().values().toArray();
        ReflectionUtils.invokeMethod(testMethod, target, args);
        return new ExecutionResult(PASSED);
    }
}
