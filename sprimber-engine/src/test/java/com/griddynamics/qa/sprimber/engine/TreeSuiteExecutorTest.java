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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.BROKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author fparamonov
 */

public class TreeSuiteExecutorTest {

    private TreeSuiteExecutor treeSuiteExecutor;
    private final TestCaseBuilder testCaseBuilder = new TestCaseBuilder();
    private final StubbedEventPublisher stubbedEventPublisher = Mockito.spy(new StubbedEventPublisher());
    private final StubbedNodeInvoker stubbedNodeInvoker = Mockito.spy(new StubbedNodeInvoker());

    @Before
    public void setUp() throws Exception {
        ErrorMapper errorMapper = Mockito.mock(ErrorMapper.class);
        NodeFallbackManager nodeFallbackManager = Mockito.mock(NodeFallbackManager.class);
        Map<String, Executor> childSubNodesExecutor = new HashMap<>();

        childSubNodesExecutor.put("testExecutor", Executors.newFixedThreadPool(3));


        Mockito.when(errorMapper.parseThrowable(any(RuntimeException.class))).thenReturn(new ExecutionResult(BROKEN));
        Mockito.when(nodeFallbackManager.parseThrowable(any(RuntimeException.class))).thenReturn(Node.Status.ERROR);
        treeSuiteExecutor = Mockito.spy(
                new TreeSuiteExecutor(stubbedNodeInvoker, nodeFallbackManager, childSubNodesExecutor, stubbedEventPublisher));
    }

    @Test
    public void smoke() {
        Node testCase = testCaseBuilder.buildTestCase();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(testCase);
        inOrder.verify(stubbedNodeInvoker, times(3)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).step();
        inOrder.verify(stubbedNodeInvoker, times(3)).after();
    }

    @Test
    public void smokeTwoSteps() {
        Node testCase = testCaseBuilder.buildTestCaseWithTestWithTwoStep();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(testCase);
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
    }

    @Test
    public void smokeTwoTestWithTwoStep() {
        Node testCase = testCaseBuilder.buildTestCaseWithTwoTestWithTwoStep();
//        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(testCase);
        verify(stubbedNodeInvoker, times(7)).before();
        verify(stubbedNodeInvoker, times(4)).step();
        verify(stubbedNodeInvoker, times(7)).after();
    }

    @Test
    public void smokeTwoStepsDoubleHooks() {
        Node testCase = testCaseBuilder.buildTestCaseWithTestWithTwoStepDoubleHooks();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(testCase);
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
    }

    @Test
    public void justException() {
        Node testCase = testCaseBuilder.buildTestCaseWithJustException();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        Node.Status result = treeSuiteExecutor.executeRoot(testCase);
        Assertions.assertThat(result).isEqualTo(Node.Status.ERROR);
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
    }

    @Test
    public void smokeException() {
        Node testCase = testCaseBuilder.buildTestCaseWithException();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        Node.Status result = treeSuiteExecutor.executeRoot(testCase);
        Assertions.assertThat(result).isEqualTo(Node.Status.ERROR);
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker, times(0)).before();
        inOrder.verify(stubbedNodeInvoker, times(0)).step();
        inOrder.verify(stubbedNodeInvoker, times(5)).skippedStep();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
    }

    @Test
    public void exceptionInBeforeHook() {
        Node testCase = testCaseBuilder.buildTestCaseWithBeforeStepException();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        Node.Status result = treeSuiteExecutor.executeRoot(testCase);
        Assertions.assertThat(result).isEqualTo(Node.Status.ERROR);
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker, times(0)).before();
        inOrder.verify(stubbedNodeInvoker, times(0)).step();
        inOrder.verify(stubbedNodeInvoker, times(5)).skippedStep();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
    }

    @Test
    public void exceptionInBeforeHookSkipOnError() {
        Node testCase = testCaseBuilder.buildTestCaseWithBeforeStepExceptionSkipOnError();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        Node.Status result = treeSuiteExecutor.executeRoot(testCase);
        Assertions.assertThat(result).isEqualTo(Node.Status.ERROR);
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).before();
        inOrder.verify(stubbedNodeInvoker).step();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker, times(0)).step();
        inOrder.verify(stubbedNodeInvoker, times(0)).before();
        inOrder.verify(stubbedNodeInvoker, times(0)).step();
        inOrder.verify(stubbedNodeInvoker, times(8)).skippedStep();
        inOrder.verify(stubbedNodeInvoker).after();
        inOrder.verify(stubbedNodeInvoker).after();
    }
}
