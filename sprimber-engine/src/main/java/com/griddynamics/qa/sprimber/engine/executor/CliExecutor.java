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

import com.griddynamics.qa.sprimber.discovery.testsuite.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.discovery.testsuite.support.TestSuiteDiscovery;
import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

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

    private final TestSuiteDefinition.TestExecutor classicTestExecutor;
    private final List<TestSuiteDiscovery> testSuiteDiscoveries;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<TestSuiteDefinition> testSuiteDefinitions = testSuiteDiscoveries.stream()
                .map(TestSuiteDiscovery::discover)
                .collect(Collectors.toList());
        testSuiteDefinitions.forEach(this::executeTestSuite);
    }

    public CompletableFuture<ExecutionResult> executeTestSuite(TestSuiteDefinition testSuiteDefinition) {
        testSuiteDefinition.getTestCaseDefinitions().forEach(this::executeTestCase);
        return CompletableFuture.completedFuture(new ExecutionResult(ExecutionResult.Status.PASSED));
    }

    public CompletableFuture<ExecutionResult> executeTestCase(TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
        testCaseDefinition.getTestDefinitions()
                .forEach(classicTestExecutor::execute);
        return CompletableFuture.completedFuture(new ExecutionResult(ExecutionResult.Status.PASSED));
    }

    public class TestCaseStartedEvent extends TestCaseEvent {

        public TestCaseStartedEvent(Object source) {
            super(source);
        }

        public TestCaseStartedEvent(Object source, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
            super(source, testCaseDefinition);
        }
    }

    public class TestCaseFinishedEvent extends TestCaseEvent {

        private ExecutionResult executionResult;

        public TestCaseFinishedEvent(Object source) {
            super(source);
        }

        public TestCaseFinishedEvent(Object source, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
            super(source, testCaseDefinition);
        }

        public ExecutionResult getExecutionResult() {
            return executionResult;
        }

        public void setExecutionResult(ExecutionResult executionResult) {
            this.executionResult = executionResult;
        }
    }

    @Getter
    @Setter
    public class TestCaseEvent extends ApplicationEvent {

        private TestSuiteDefinition.TestCaseDefinition testCaseDefinition;

        /**
         * Create a new ApplicationEvent.
         *
         * @param source the object on which the event initially occurred (never {@code null})
         */
        public TestCaseEvent(Object source) {
            super(source);
        }

        public TestCaseEvent(Object source, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
            super(source);
            this.testCaseDefinition = testCaseDefinition;
        }
    }
}
