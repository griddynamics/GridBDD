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

package com.griddynamics.qa.sprimber.discovery.testsuite.support;

import com.griddynamics.qa.sprimber.discovery.testsuite.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.engine.executor.ErrorMapper;
import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static com.griddynamics.qa.sprimber.engine.model.ExecutionResult.Status.PASSED;

/**
 * @author fparamonov
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicTestExecutor implements TestSuiteDefinition.TestExecutor {

    private final ApplicationContext applicationContext;
    private final ApplicationEventPublisher eventPublisher;
    private final ErrorMapper errorMapper;

    @Override
    public CompletableFuture<ExecutionResult> execute(TestSuiteDefinition.TestDefinition testDefinition) {
        try {
            TestStartedEvent testStartedEvent = new TestStartedEvent(this, testDefinition);
            eventPublisher.publishEvent(testStartedEvent);
            Method testMethod = testDefinition.getStepDefinitions().get(0).getMethod();
            Object target = applicationContext.getBean(testMethod.getDeclaringClass());
            ReflectionUtils.invokeMethod(testMethod, target);
            ExecutionResult result = new ExecutionResult(PASSED);
            TestFinishedEvent testFinishedEvent = new TestFinishedEvent(this, testDefinition);
            testDefinition.setExecutionResult(result);
            eventPublisher.publishEvent(testFinishedEvent);
            return CompletableFuture.completedFuture(result);
        } catch (Throwable throwable) {
            ExecutionResult result = errorMapper.parseThrowable(throwable);
            result.conditionallyPrintStacktrace();
            log.trace(result.getErrorMessage());
            log.error(throwable.getLocalizedMessage());
            return CompletableFuture.completedFuture(result);
        }
    }

    public class TestStartedEvent extends TestEvent {

        public TestStartedEvent(Object source) {
            super(source);
        }

        public TestStartedEvent(Object source, TestSuiteDefinition.TestDefinition testDefinition) {
            super(source, testDefinition);
        }
    }

    public class TestFinishedEvent extends TestEvent {

        public TestFinishedEvent(Object source) {
            super(source);
        }

        public TestFinishedEvent(Object source, TestSuiteDefinition.TestDefinition testDefinition) {
            super(source, testDefinition);
        }
    }

    @Getter
    @Setter
    public class TestEvent extends ApplicationEvent {

        private TestSuiteDefinition.TestDefinition testDefinition;

        /**
         * Create a new ApplicationEvent.
         *
         * @param source the object on which the event initially occurred (never {@code null})
         */
        public TestEvent(Object source) {
            super(source);
        }

        public TestEvent(Object source, TestSuiteDefinition.TestDefinition testDefinition) {
            super(source);
            this.testDefinition = testDefinition;
        }
    }
}
