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

package com.griddynamics.qa.sprimber.engine.executor;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.discovery.support.StepDefinitionsDiscovery;
import com.griddynamics.qa.sprimber.discovery.support.classic.ClassicDiscovery;
import com.griddynamics.qa.sprimber.discovery.support.cucumber.CucumberFeaturesDiscovery;
import com.griddynamics.qa.sprimber.engine.ExecutionContext;
import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CliExecutor implements ApplicationRunner {

    private final ExecutionContext executionContext;
    private final TestSuiteDefinition.TestExecutor classicTestExecutor;
    private final TestSuiteDefinition.TestExecutor bddTestExecutor;
    private final ClassicDiscovery classicDiscovery;
    private final CucumberFeaturesDiscovery cucumberFeaturesDiscovery;
    private final List<StepDefinitionsDiscovery> stepDefinitionsDiscoveries;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<StepDefinition> stepDefinitions = stepDefinitionsDiscoveries.stream()
                .map(StepDefinitionsDiscovery::discover)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        executionContext.getStepDefinitions().addAll(stepDefinitions);
        List<TestSuiteDefinition> classicTestSuites = Collections.singletonList(classicDiscovery.discover());
        List<TestSuiteDefinition> bddTestSuites = Collections.singletonList(cucumberFeaturesDiscovery.discover());
        classicTestSuites.forEach(testSuiteDefinition -> executeTestSuite(testSuiteDefinition, classicTestExecutor));
        bddTestSuites.forEach(testSuiteDefinition -> executeTestSuite(testSuiteDefinition, bddTestExecutor));
    }

    public CompletableFuture<ExecutionResult> executeTestSuite(TestSuiteDefinition testSuiteDefinition,
                                                               TestSuiteDefinition.TestExecutor testExecutor) {
        testSuiteDefinition.getTestCaseDefinitions().forEach(testCaseDefinition -> executeTestCase(testCaseDefinition, testExecutor));
        return CompletableFuture.completedFuture(new ExecutionResult(ExecutionResult.Status.PASSED));
    }

    public CompletableFuture<ExecutionResult> executeTestCase(TestSuiteDefinition.TestCaseDefinition testCaseDefinition,
                                                              TestSuiteDefinition.TestExecutor testExecutor) {
        testCaseDefinition.getTestDefinitions()
                .forEach(testExecutor::execute);
        return CompletableFuture.completedFuture(new ExecutionResult(ExecutionResult.Status.PASSED));
    }
}
