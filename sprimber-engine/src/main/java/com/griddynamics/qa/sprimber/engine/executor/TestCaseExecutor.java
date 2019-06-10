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

import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.model.TestCase;
import com.griddynamics.qa.sprimber.engine.model.TestStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static com.griddynamics.qa.sprimber.engine.model.ExecutionResult.Status.PASSED;
import static com.griddynamics.qa.sprimber.engine.model.ThreadConstants.SPRIMBER_TC_EXECUTOR_NAME;

/**
 * This class play a role of test case orchestrator. Main goal here to schedule execution in right order.
 * This is a one main entry point to test case execution
 *
 * @author fparamonov
 */

@Component
public class TestCaseExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseExecutor.class);

    private final TestCaseActionsExecutor actionsExecutor;

    public TestCaseExecutor(TestCaseActionsExecutor actionsExecutor) {
        this.actionsExecutor = actionsExecutor;
    }

    @Async(SPRIMBER_TC_EXECUTOR_NAME)
    public CompletableFuture<ExecutionResult> execute(TestCase testCase) {
        ExecutionResult testCaseResult = new ExecutionResult(PASSED);

        // TODO: 11/17/18 Make stage management more graceful
        actionsExecutor.cleanStageResults();
        testCase.getBeforeScenarioActions().forEach(actionsExecutor::executeHookAction);
        if (actionsExecutor.currentFailures().isEmpty()) {
            for (TestStep testStep : testCase.getSteps()) {
                actionsExecutor.cleanStageResults();
                testCase.getBeforeStepActions().forEach(actionsExecutor::executeHookAction);
                if (!actionsExecutor.currentFailures().isEmpty()) {
                    testCaseResult = actionsExecutor.currentFailures().get(0);
                    break;
                }
                actionsExecutor.cleanStageResults();
                actionsExecutor.executeStepAction(testStep);
                if (!actionsExecutor.currentFailures().isEmpty()) {
                    testCaseResult = actionsExecutor.currentFailures().get(0);
                    break;
                }
                actionsExecutor.cleanStageResults();
                testCase.getAfterStepActions().forEach(actionsExecutor::executeHookAction);
                if (!actionsExecutor.currentFailures().isEmpty()) {
                    testCaseResult = actionsExecutor.currentFailures().get(0);
                    break;
                }
            }
        } else {
            testCaseResult = actionsExecutor.currentFailures().get(0);
        }
        actionsExecutor.cleanStageResults();
        testCase.getAfterScenarioActions().forEach(actionsExecutor::executeHookAction);
        if (!actionsExecutor.currentFailures().isEmpty()) {
            testCaseResult = actionsExecutor.currentFailures().get(0);
        }

        return CompletableFuture.completedFuture(testCaseResult);
    }
}
