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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static com.griddynamics.qa.sprimber.engine.model.ExecutionResult.Status.PASSED;
import static com.griddynamics.qa.sprimber.engine.model.ThreadConstants.SPRIMBER_EXECUTOR_NAME;

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
    private CountDownLatch countDownLatch;

    public TestCaseExecutor(TestCaseActionsExecutor actionsExecutor) {
        this.actionsExecutor = actionsExecutor;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Async(SPRIMBER_EXECUTOR_NAME)
    public ExecutionResult execute(TestCase testCase) {
        ExecutionResult testCaseResult = new ExecutionResult(PASSED);
        // TODO: 10/06/2018 make countdown latch handling more attractive
        LOGGER.debug("Remaining TC count {}", countDownLatch.getCount());

        actionsExecutor.cleanStageResults();
        testCase.getBeforeActions().forEach(actionsExecutor::executeHookAction);
        if (actionsExecutor.currentFailures().isEmpty()) {
            actionsExecutor.cleanStageResults();
            // TODO: 11/15/18 implement graceful feedback mechanism/backpressure to producer
            testCase.getSteps().stream()
                    .map(actionsExecutor::executeStep)
                    .allMatch(executionResult -> executionResult.getStatus().equals(PASSED));
        } else {
            testCaseResult = actionsExecutor.currentFailures().get(0);
        }
        if (!actionsExecutor.currentFailures().isEmpty()) {
            testCaseResult = actionsExecutor.currentFailures().get(0);
        }
        actionsExecutor.cleanStageResults();
        testCase.getAfterActions().forEach(actionsExecutor::executeHookAction);
        if (!actionsExecutor.currentFailures().isEmpty()) {
            testCaseResult = actionsExecutor.currentFailures().get(0);
        }
        countDownLatch.countDown();
        LOGGER.debug("Remaining TC count {}", countDownLatch.getCount());
        return testCaseResult;
    }
}
