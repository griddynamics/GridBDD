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

import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.BROKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

/**
 * @author fparamonov
 */

public class TreeSuiteExecutorTest {

    private TreeSuiteExecutor treeSuiteExecutor;
    private final TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

    @Before
    public void setUp() throws Exception {
        ErrorMapper errorMapper = Mockito.mock(ErrorMapper.class);
        TreeSuiteExecutor.NodeFallbackManager nodeFallbackManager = Mockito.mock(TreeSuiteExecutor.NodeFallbackManager.class);

        Mockito.when(errorMapper.parseThrowable(any(RuntimeException.class))).thenReturn(new ExecutionResult(BROKEN));
        Mockito.when(nodeFallbackManager.parseThrowable(any(RuntimeException.class))).thenReturn(TreeSuiteExecutor.Node.Status.ERROR);
        treeSuiteExecutor = Mockito.spy(new TreeSuiteExecutor(errorMapper, nodeFallbackManager));
    }

    @Test
    public void smoke() {
        TreeSuiteExecutor.Node testCase = testCaseBuilder.buildTestCase();
        treeSuiteExecutor.executeNode(testCase);
        InOrder inOrder = Mockito.inOrder(treeSuiteExecutor);
        inOrder.verify(treeSuiteExecutor, times(3)).before();
        inOrder.verify(treeSuiteExecutor, times(1)).step();
        inOrder.verify(treeSuiteExecutor, times(3)).after();
    }

    @Test
    public void smokeTwoSteps() {
        TreeSuiteExecutor.Node testCase = testCaseBuilder.buildTestCaseWithTestWithTwoStep();
        treeSuiteExecutor.executeNode(testCase);
        InOrder inOrder = Mockito.inOrder(treeSuiteExecutor);
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).step();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).step();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
    }

    @Test
    public void smokeTwoStepsDoubleHooks() {
        TreeSuiteExecutor.Node testCase = testCaseBuilder.buildTestCaseWithTestWithTwoStepDoubleHooks();
        treeSuiteExecutor.executeNode(testCase);
        InOrder inOrder = Mockito.inOrder(treeSuiteExecutor);
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).step();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).step();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
    }

    @Test
    public void justException() {
        TreeSuiteExecutor.Node testCase = testCaseBuilder.buildTestCaseWithJustException();
        ExecutionResult result = treeSuiteExecutor.executeNode(testCase);
        Assertions.assertThat(result.getStatus()).isEqualTo(BROKEN);
        InOrder inOrder = Mockito.inOrder(treeSuiteExecutor);
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).exceptionalStep();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
    }

    @Test
    public void smokeException() {
        TreeSuiteExecutor.Node testCase = testCaseBuilder.buildTestCaseWithException();
        ExecutionResult result = treeSuiteExecutor.executeNode(testCase);
        Assertions.assertThat(result.getStatus()).isEqualTo(BROKEN);
        InOrder inOrder = Mockito.inOrder(treeSuiteExecutor);
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).step();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).before();
        inOrder.verify(treeSuiteExecutor).exceptionalStep();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor, times(0)).before();
        inOrder.verify(treeSuiteExecutor, times(0)).step();
        inOrder.verify(treeSuiteExecutor).after();
        inOrder.verify(treeSuiteExecutor).after();
    }

    @Test
    public void toDelete() {
        for (int i = 0; i < TreeSuiteExecutor.Node.Status.values().length; i++) {
            TreeSuiteExecutor.Node.Status status = TreeSuiteExecutor.Node.Status.values()[i];
            System.out.println(status);
            System.out.println(Integer.toBinaryString(status.value));
            System.out.println(Integer.toHexString(status.value));
        }
    }
}
