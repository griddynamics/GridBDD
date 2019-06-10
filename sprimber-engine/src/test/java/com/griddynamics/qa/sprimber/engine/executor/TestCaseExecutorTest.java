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
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

/**
 * @author fparamonov
 */
public class TestCaseExecutorTest {

    private TestCaseActionsExecutor actionsExecutor;
    private TestCaseExecutor testCaseExecutor;

    @Before
    public void setUp() throws Exception {
        actionsExecutor = Mockito.mock(TestCaseActionsExecutor.class);
        testCaseExecutor = new TestCaseExecutor(actionsExecutor);
    }

    @Test
    public void onePositiveBeforeScenarioHook() throws Exception {
        TestCase testCase = new TestCase();
        testCase.getAllHooks().add(MockActionBuilder.mockBeforeScenarioAction());
        CompletableFuture<ExecutionResult> resultFuture = testCaseExecutor.execute(testCase);
        Assertions.assertThat(resultFuture.get().getStatus()).as("Test case finished with wrong status").isEqualTo(ExecutionResult.Status.PASSED);
        Mockito.verify(actionsExecutor, Mockito.never()).executeStepAction(ArgumentMatchers.any());
        Mockito.verify(actionsExecutor, Mockito.times(1)).executeHookAction(ArgumentMatchers.any());
    }

    @Test
    public void onePositiveStepSurroundedByHooks() throws Exception {
        TestCase testCase = new TestCase();
        testCase.getAllHooks().add(MockActionBuilder.mockBeforeScenarioAction());
        testCase.getAllHooks().add(MockActionBuilder.mockAfterScenarioAction());
        testCase.getAllHooks().add(MockActionBuilder.mockBeforeStepAction());
        testCase.getAllHooks().add(MockActionBuilder.mockAfterStepAction());
        testCase.getSteps().add(MockStepBuilder.mockGivenStep());

        CompletableFuture<ExecutionResult> resultFuture = testCaseExecutor.execute(testCase);
        ExecutionResult result = resultFuture.get();
        Assertions.assertThat(result.getStatus()).as("Test case finished with wrong status").isEqualTo(ExecutionResult.Status.PASSED);
        Mockito.verify(actionsExecutor, Mockito.times(1)).executeStepAction(ArgumentMatchers.any());
        Mockito.verify(actionsExecutor, Mockito.times(4)).executeHookAction(ArgumentMatchers.any());
    }
}
