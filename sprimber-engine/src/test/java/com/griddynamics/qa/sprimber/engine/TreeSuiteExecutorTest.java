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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.times;

/**
 * @author fparamonov
 */

public class TreeSuiteExecutorTest {

    private TreeSuiteExecutor treeSuiteExecutor;
    private final TestCaseBuilder testCaseBuilder = new TestCaseBuilder();
    private final TreeExecutorContext context = new TreeExecutorContext();
    private final StubbedEventPublisher stubbedEventPublisher = Mockito.spy(new StubbedEventPublisher());
    private final StubbedNodeInvoker stubbedNodeInvoker = Mockito.spy(new StubbedNodeInvoker());

    @Before
    public void setUp() throws Exception {
        Map<String, Executor> childSubNodesExecutor = new HashMap<>();

        childSubNodesExecutor.put("testExecutor", Executors.newFixedThreadPool(3));
        childSubNodesExecutor.put("node-async-pool", Executors.newFixedThreadPool(3));

        treeSuiteExecutor = Mockito.spy(
                new TreeSuiteExecutor(stubbedNodeInvoker, childSubNodesExecutor, context, stubbedEventPublisher));
    }

    @Test
    public void singleStepNode() {
        Node singleStepNode = testCaseBuilder.buildSingleStep();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(singleStepNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).step();
    }

    @Test
    public void singleWrappedStep() {
        Node singleStepNode = testCaseBuilder.buildSingleWrappedStep();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(singleStepNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).step();
        inOrder.verify(stubbedNodeInvoker, times(1)).after();
    }

    @Test
    public void regularAndExceptionalStep() {
        Node testNode = testCaseBuilder.buildTestWithRegularAndExceptionalWrappedStep();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(testNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker, times(1)).after();
    }

    @Test
    public void singleWrappedStepWithExceptionalBefore() {
        Node stepNode = testCaseBuilder.buildSingleWrappedStepWithExceptionalBefore();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(stepNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker, times(0)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).step();
        inOrder.verify(stubbedNodeInvoker, times(1)).after();
    }

    @Test
    public void singleWrappedStepWithErrorOnBeforeSkipTarget() {
        Node stepNode = testCaseBuilder.buildSingleWrappedStepWithExceptionalBeforeSkipTarget();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(stepNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker, times(0)).step();
        inOrder.verify(stubbedNodeInvoker, times(1)).after();
    }

    @Test
    public void singleWrappedStepWithErrorOnBeforeSkipTargetAndAfter() {
        Node stepNode = testCaseBuilder.buildSingleWrappedStepWithExceptionalBeforeSkipTargetAndAfter();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(stepNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker, times(0)).step();
        inOrder.verify(stubbedNodeInvoker, times(0)).after();
    }

    @Test
    public void singleWrappedStepWithExceptionalAfter() {
        Node stepNode = testCaseBuilder.buildSingleWrappedStepWithExceptionalAfter();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(stepNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).step();
        inOrder.verify(stubbedNodeInvoker, times(1)).after();
        inOrder.verify(stubbedNodeInvoker, times(1)).exceptionalStep();
        inOrder.verify(stubbedNodeInvoker, times(0)).after();
    }

    @Test
    public void asyncAndRegularStep() {
        Node rootNode = testCaseBuilder.buildAsyncAndRegularWrappedSteps();
        InOrder inOrder = Mockito.inOrder(stubbedNodeInvoker);
        treeSuiteExecutor.executeRoot(rootNode);
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).step();
        inOrder.verify(stubbedNodeInvoker, times(1)).after();
        inOrder.verify(stubbedNodeInvoker, times(1)).before();
        inOrder.verify(stubbedNodeInvoker, times(1)).step();
        inOrder.verify(stubbedNodeInvoker, times(1)).after();
    }
}
