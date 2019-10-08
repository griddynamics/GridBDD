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

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.griddynamics.qa.sprimber.engine.Node.*;

/**
 * @author fparamonov
 */

@Slf4j
public class TestCaseBuilder {

    private final Method beforeMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "before");
    private final Method stepMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "step");
    private final Method afterMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "after");
    private final Method exceptionalStepMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "exceptionalStep");

    Node buildTestCase() {
        Node.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        Node testNode = buildTestNodeWithOneSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    Node buildTestCaseWithTestWithTwoStep() {
        Node.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        Node testNode = buildTestNodeWithTwoSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    Node buildTestCaseWithTestWithTwoStepDoubleHooks() {
        Node.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        Node testNode = buildTestNodeWithTwoSurroundStepDoubleHooks();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    Node buildTestCaseWithException() {
        Node.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        Node testNode = buildTestWithRegularAndExceptionalSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    Node buildTestCaseWithBeforeStepException() {
        Node.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        Node testNode = buildTestWithTwoSurroundStepAndBeforeStepException();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    Node buildTestCaseWithBeforeStepExceptionSkipOnError() {
        Node.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        Node testNode = buildTestWithTwoSurroundStepAndBeforeStepExceptionSkipOnError();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    Node buildTestCaseWithJustException() {
        Node.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        Node testNode = buildTestWithJustExceptionalSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    Node buildTestNodeWithOneSurroundStep() {
        Node testNode = testNode();
        Node stepNode = buildStepSurroundWithSingleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(stepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    Node buildTestNodeWithTwoSurroundStep() {
        Node testNode = testNode();
        Node firstStepNode = buildStepSurroundWithSingleHooks();
        Node secondStepNode = buildStepSurroundWithSingleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    Node buildTestNodeWithTwoSurroundStepDoubleHooks() {
        Node testNode = testNode();
        Node firstStepNode = buildStepSurroundWithDoubleHooks();
        Node secondStepNode = buildStepSurroundWithDoubleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    Node buildTestWithRegularAndExceptionalSurroundStep() {
        Node testNode = testNode();
        Node firstStepNode = buildStepSurroundWithSingleHooks();
        Node secondStepNode = buildExceptionalStepSurroundWithSingleHooks();
        Node thirdStepNode = buildStepSurroundWithDoubleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().get("child").add(thirdStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    Node buildTestWithTwoSurroundStepAndBeforeStepException() {
        Node testNode = testNode();
        Node firstStepNode = buildStepSurroundWithSingleHooks();
        Node secondStepNode = buildSurroundStepWithBeforeException();
        Node thirdStepNode = buildStepSurroundWithDoubleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().get("child").add(thirdStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    Node buildTestWithTwoSurroundStepAndBeforeStepExceptionSkipOnError() {
        Node testNode = testNode();
        Node firstStepNode = buildStepSurroundWithSingleHooks();
        Node secondStepNode = buildSurroundStepWithBeforeExceptionDryOnError();
        Node thirdStepNode = buildStepSurroundWithDoubleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().get("child").add(thirdStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    Node buildTestWithJustExceptionalSurroundStep() {
        Node testNode = testNode();
        Node stepNode = buildExceptionalStepSurroundWithSingleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(stepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    Node buildStepSurroundWithSingleHooks() {
        Node stepNode = stepNode();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(targetNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return stepNode;
    }

    Node buildExceptionalStepSurroundWithSingleHooks() {
        Node stepNode = stepNode();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(exceptionalStepNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return stepNode;
    }

    Node buildSurroundStepWithBeforeException() {
        Node stepNode = stepNode();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(exceptionalStepNode());
        stepNode.getChildren().get("before").add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(targetNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return stepNode;
    }

    Node buildSurroundStepWithBeforeExceptionDryOnError() {
        Node stepNode = stepNodeErrorPolicy();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(exceptionalStepNode());
        stepNode.getChildren().get("before").add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(targetNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return stepNode;
    }

    Node buildStepSurroundWithDoubleHooks() {
        Node stepNode = stepNode();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        stepNode.getChildren().get("before").add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(targetNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        stepNode.getChildren().get("after").add(afterNode());
        return stepNode;
    }

    private Node.ExecutableNode beforeNode() {
        Node.ExecutableNode executableNode = new Node.ExecutableNode();
        executableNode.setMethod(beforeMethod);
        return executableNode;
    }

    private Node.ExecutableNode afterNode() {
        Node.ExecutableNode executableNode = new Node.ExecutableNode();
        executableNode.setMethod(afterMethod);
        return executableNode;
    }

    private Node.ExecutableNode targetNode() {
        Node.ExecutableNode executableNode = new Node.ExecutableNode();
        executableNode.setMethod(stepMethod);
        return executableNode;
    }

    private Node.ExecutableNode exceptionalStepNode() {
        Node.ExecutableNode executableNode = new Node.ExecutableNode();
        executableNode.setMethod(exceptionalStepMethod);
        return executableNode;
    }

    private Node.ContainerNode testNode() {
        return new ContainerNode(DRY_BEFORES_ON_DRY | DRY_AFTERS_ON_DRY | DRY_TARGETS_ON_DRY | DRY_CHILDES_ON_ERROR);
    }

    private Node.ContainerNode stepNode() {
        return new ContainerNode(DRY_BEFORES_ON_DRY | DRY_AFTERS_ON_DRY | DRY_TARGETS_ON_DRY);
    }

    private Node.ContainerNode stepNodeErrorPolicy() {
        return new ContainerNode(DRY_BEFORES_ON_DRY | DRY_AFTERS_ON_DRY | DRY_TARGETS_ON_DRY | DRY_CHILDES_ON_ERROR
                | DRY_BEFORES_ON_ERROR | DRY_TARGETS_ON_ERROR | DRY_AFTERS_ON_ERROR);
    }

    private Node.ContainerNode childNode() {
        return new Node.ContainerNode();
    }
}
