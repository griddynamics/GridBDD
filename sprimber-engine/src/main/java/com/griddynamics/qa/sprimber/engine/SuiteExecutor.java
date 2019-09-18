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

import com.griddynamics.qa.sprimber.discovery.TestSuite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SuiteExecutor {

    private final Executor sprimberTestCaseExecutor;

    public CompletableFuture<ExecutionResult> executeTestSuite(TestSuite testSuite) {

        List<CompletableFuture<TestSuite.TestCase>> testCases = testSuite.getTestCases().stream()
                .map(testCaseDefinition ->
                        CompletableFuture
                                .supplyAsync(() -> executeTestCase(testCaseDefinition, testSuite.getTestExecutor()), sprimberTestCaseExecutor)
                                .thenApply(result -> {
                                            testCaseDefinition.setExecutionResult(result);
                                            return testCaseDefinition;
                                        }
                                ))
                .collect(Collectors.toList());
        CompletionStage<List<TestSuite.TestCase>> executedTestCases =
                CompletableFuture.allOf(testCases.toArray(new CompletableFuture[0]))
                        .thenApply(v -> testCases.stream()
                                .map(CompletionStage::toCompletableFuture)
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
                        );
        executedTestCases.whenComplete(((testCaseDefinitions, throwable) -> testCaseDefinitions.stream()
                .map(TestSuite.TestCase::getExecutionResult)
                .map(ExecutionResult::getStatus)
                .forEach(status -> log.info("Test Case Status: {}", status))
        )).toCompletableFuture().join();
        return CompletableFuture.completedFuture(new ExecutionResult(ExecutionResult.Status.PASSED));
    }

    public ExecutionResult executeTestCase(TestSuite.TestCase testCase,
                                           TestSuite.TestExecutor testExecutor) {
        Map<ExecutionResult.Status, Long> statistic = new HashMap<>();
        List<CompletionStage<TestSuite.Test>> executedTests = testCase.getTests().stream()
                .map(testDefinition ->
                        testExecutor.execute(testDefinition)
                                .thenApply(result -> {
                                    testDefinition.setExecutionResult(result);
                                    return testDefinition;
                                }))
                .collect(Collectors.toList());
        CompletableFuture<Void> done = CompletableFuture.allOf(executedTests.toArray(new CompletableFuture[executedTests.size()]));
        CompletionStage<List<TestSuite.Test>> updatedTests = done.thenApply(v -> executedTests.stream()
                .map(CompletionStage::toCompletableFuture)
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
        updatedTests.whenComplete((testDefinitions, throwable) -> statistic.putAll(testDefinitions.stream()
                .map(TestSuite.Test::getExecutionResult)
                .collect(Collectors.groupingBy(ExecutionResult::getStatus, Collectors.counting())))
        ).toCompletableFuture().join();
        // TODO: 2019-09-14 add the test case name here
        log.info("Statistic: {}", statistic);
        return new ExecutionResult(ExecutionResult.Status.PASSED);
    }
}