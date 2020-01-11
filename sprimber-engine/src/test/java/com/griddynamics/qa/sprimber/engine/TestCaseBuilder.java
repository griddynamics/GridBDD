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

    Node buildSingleStep() {
        Node stepNode = Node.createRootNode("stepRoot", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_TARGET_WHEN_BYPASS_MODE);
        stepNode.addTarget("step", stepMethod);
        return stepNode;
    }

    Node buildSingleWrappedStep() {
        Node stepNode = Node.createRootNode("stepRoot", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_TARGET_WHEN_BYPASS_MODE);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildTestWithRegularAndExceptionalWrappedStep() {
        Node testNode = Node.createRootNode("testRoot", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE |
                BYPASS_TARGET_WHEN_BYPASS_MODE | BYPASS_CHILDREN_AFTER_ITERATION_ERROR);
        Node exceptionalStepNode = testNode.addChild("stepHolder", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE |
                BYPASS_TARGET_WHEN_BYPASS_MODE);
        exceptionalStepNode.addBefore("before", beforeMethod);
        exceptionalStepNode.addTarget("step", exceptionalStepMethod);
        exceptionalStepNode.addAfter("after", afterMethod);
        Node regularStepNode = testNode.addChild("stepHolder", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE |
                BYPASS_TARGET_WHEN_BYPASS_MODE);
        regularStepNode.addBefore("before", beforeMethod);
        regularStepNode.addTarget("step", stepMethod);
        regularStepNode.addAfter("after", afterMethod);
        return testNode;
    }

    Node buildSingleWrappedStepWithExceptionalBefore() {
        Node stepNode = Node.createRootNode("stepRoot", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE |
                BYPASS_TARGET_WHEN_BYPASS_MODE | BYPASS_BEFORE_AFTER_ITERATION_ERROR);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addBefore("before", exceptionalStepMethod);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalAfter() {
        Node stepNode = Node.createRootNode("stepRoot", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE |
                BYPASS_TARGET_WHEN_BYPASS_MODE | BYPASS_AFTER_AFTER_ITERATION_ERROR);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        stepNode.addAfter("after", exceptionalStepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTarget() {
        Node stepNode = Node.createRootNode("stepRoot", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE |
                BYPASS_TARGET_WHEN_BYPASS_MODE | BYPASS_BEFORE_AFTER_ITERATION_ERROR | BYPASS_TARGET_AFTER_STAGE_ERROR);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addBefore("before", exceptionalStepMethod);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTargetAndAfter() {
        Node stepNode = Node.createRootNode("stepRoot", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE |
                BYPASS_TARGET_WHEN_BYPASS_MODE | BYPASS_BEFORE_AFTER_ITERATION_ERROR | BYPASS_TARGET_AFTER_STAGE_ERROR | BYPASS_AFTER_AFTER_STAGE_ERROR);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addBefore("before", exceptionalStepMethod);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

//    Node buildTestCase() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node testNode = buildTestNodeWithOneSurroundStep();
//        testCaseNode.addChild(testNode);
//        return testCaseNode;
//    }
//
//    Node buildTestCaseWithTestWithTwoStep() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node testNode = buildTestNodeWithTwoSurroundStep();
//        testCaseNode.addChild(testNode);
//        return testCaseNode;
//    }
//
//    Node buildTestCaseWithTwoTestWithTwoStep() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node firstTestNode = buildTestNodeWithTwoSurroundStep();
//        Node secondTestNode = buildTestNodeWithTwoSurroundStep();
//        testCaseNode.addChild(firstTestNode);
//        testCaseNode.addChild(secondTestNode);
//        return testCaseNode;
//    }
//
//    Node buildTestCaseWithTestWithTwoStepDoubleHooks() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node testNode = buildTestNodeWithTwoSurroundStepDoubleHooks();
//        testCaseNode.addChild(testNode);
//        return testCaseNode;
//    }
//
//    Node buildTestCaseWithException() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node testNode = buildTestWithRegularAndExceptionalSurroundStep();
//        testCaseNode.addChild(testNode);
//        return testCaseNode;
//    }
//
//    Node buildTestCaseWithBeforeStepException() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node testNode = buildTestWithTwoSurroundStepAndBeforeStepException();
//        testCaseNode.addChild(testNode);
//        return testCaseNode;
//    }
//
//    Node buildTestCaseWithBeforeStepExceptionSkipOnError() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node testNode = buildTestWithTwoSurroundStepAndBeforeStepExceptionSkipOnError();
//        testCaseNode.addChild(testNode);
//        return testCaseNode;
//    }
//
//    Node buildTestCaseWithJustException() {
//        Node.ContainerNode testCaseNode = testCaseNode();
//        testCaseNode.addBefore(beforeNode());
//        testCaseNode.addAfter(afterNode());
//        Node testNode = buildTestWithJustExceptionalSurroundStep();
//        testCaseNode.addChild(testNode);
//        return testCaseNode;
//    }
//
//    Node buildTestNodeWithOneSurroundStep() {
//        Node testNode = testNode();
//        Node stepNode = buildStepSurroundWithSingleHooks();
//        testNode.addBefore(beforeNode());
//        testNode.addChild(stepNode);
//        testNode.addAfter(afterNode());
//        return testNode;
//    }
//
//    Node buildTestNodeWithTwoSurroundStep() {
//        Node testNode = testNode();
//        Node firstStepNode = buildStepSurroundWithSingleHooks();
//        Node secondStepNode = buildStepSurroundWithSingleHooks();
//        testNode.addBefore(beforeNode());
//        testNode.addChild(firstStepNode);
//        testNode.getChildren().get("child").add(secondStepNode);
//        testNode.addAfter(afterNode());
//        return testNode;
//    }
//
//    Node buildTestNodeWithTwoSurroundStepDoubleHooks() {
//        Node testNode = testNode();
//        Node firstStepNode = buildStepSurroundWithDoubleHooks();
//        Node secondStepNode = buildStepSurroundWithDoubleHooks();
//        testNode.addBefore(beforeNode());
//        testNode.addChild(firstStepNode);
//        testNode.addChild(secondStepNode);
//        testNode.addAfter(afterNode());
//        return testNode;
//    }
//
//    Node buildTestWithRegularAndExceptionalSurroundStep() {
//        Node testNode = testNode();
//        Node firstStepNode = buildStepSurroundWithSingleHooks();
//        Node secondStepNode = buildExceptionalStepSurroundWithSingleHooks();
//        Node thirdStepNode = buildStepSurroundWithDoubleHooks();
//        testNode.addBefore(beforeNode());
//        testNode.addChild(firstStepNode);
//        testNode.addChild(secondStepNode);
//        testNode.addChild(thirdStepNode);
//        testNode.addAfter(afterNode());
//        return testNode;
//    }
//
//    Node buildTestWithTwoSurroundStepAndBeforeStepException() {
//        Node testNode = testNode();
//        Node firstStepNode = buildStepSurroundWithSingleHooks();
//        Node secondStepNode = buildSurroundStepWithBeforeException();
//        Node thirdStepNode = buildStepSurroundWithDoubleHooks();
//        testNode.addBefore(beforeNode());
//        testNode.addChild(firstStepNode);
//        testNode.addChild(secondStepNode);
//        testNode.addChild(thirdStepNode);
//        testNode.addAfter(afterNode());
//        return testNode;
//    }
//
//    Node buildTestWithTwoSurroundStepAndBeforeStepExceptionSkipOnError() {
//        Node testNode = testNode();
//        Node firstStepNode = buildStepSurroundWithSingleHooks();
//        Node secondStepNode = buildSurroundStepWithBeforeExceptionDryOnError();
//        Node thirdStepNode = buildStepSurroundWithDoubleHooks();
//        testNode.addBefore(beforeNode());
//        testNode.addChild(firstStepNode);
//        testNode.addChild(secondStepNode);
//        testNode.addChild(thirdStepNode);
//        testNode.addAfter(afterNode());
//        return testNode;
//    }
//
//    Node buildTestWithJustExceptionalSurroundStep() {
//        Node testNode = testNode();
//        Node stepNode = buildExceptionalStepSurroundWithSingleHooks();
//        testNode.addBefore(beforeNode());
//        testNode.addChild(stepNode);
//        testNode.addAfter(afterNode());
//        return testNode;
//    }
//
//    Node buildStepSurroundWithSingleHooks() {
//        Node stepNode = stepNode();
//        stepNode.addBefore(beforeNode());
//        stepNode.addTarget(targetNode());
//        stepNode.addAfter(afterNode());
//        return stepNode;
//    }
//
//    Node buildExceptionalStepSurroundWithSingleHooks() {
//        Node stepNode = stepNode();
//        stepNode.addBefore(beforeNode());
//        stepNode.addTarget(exceptionalStepNode());
//        stepNode.addAfter(afterNode());
//        return stepNode;
//    }
//
//    Node buildSurroundStepWithBeforeException() {
//        Node stepNode = stepNode();
//        stepNode.addBefore(exceptionalStepNode());
//        stepNode.getChildren().get("before").add(beforeNode());
//        stepNode.addTarget(targetNode());
//        stepNode.addAfter(afterNode());
//        return stepNode;
//    }
//
//    Node buildSurroundStepWithBeforeExceptionDryOnError() {
//        Node stepNode = stepNodeErrorPolicy();
//        stepNode.addBefore(exceptionalStepNode());
//        stepNode.getChildren().get("before").add(beforeNode());
//        stepNode.addTarget(targetNode());
//        stepNode.addAfter(afterNode());
//        return stepNode;
//    }
//
//    Node buildStepSurroundWithDoubleHooks() {
//        Node stepNode = stepNode();
//        stepNode.addBefore(beforeNode());
//        stepNode.getChildren().get("before").add(beforeNode());
//        stepNode.addTarget(targetNode());
//        stepNode.addAfter(afterNode());
//        stepNode.getChildren().get("after").add(afterNode());
//        return stepNode;
//    }
//
//    private Node.ExecutableNode beforeNode() {
//        Node.ExecutableNode executableNode = new Node.ExecutableNode("before");
//        executableNode.setMethod(beforeMethod);
//        return executableNode;
//    }
//
//    private Node.ExecutableNode afterNode() {
//        Node.ExecutableNode executableNode = new Node.ExecutableNode("after");
//        executableNode.setMethod(afterMethod);
//        return executableNode;
//    }
//
//    private Node.ExecutableNode targetNode() {
//        Node.ExecutableNode executableNode = new Node.ExecutableNode("step");
//        executableNode.setMethod(stepMethod);
//        return executableNode;
//    }
//
//    private Node.ExecutableNode exceptionalStepNode() {
//        Node.ExecutableNode executableNode = new Node.ExecutableNode("step");
//        executableNode.setMethod(exceptionalStepMethod);
//        return executableNode;
//    }
//
//    private Node.ContainerNode testCaseNode() {
//        return new Node.ContainerNode("testCase");
//    }
//
//    private Node.ContainerNode testNode() {
//        return new ContainerNode("test",BYPASS_BEFORE_WHEN_BYPASS_MODE | DRY_AFTERS_ON_DRY | DRY_TARGETS_ON_DRY | SWITCH_TO_DRY_FOR_CHILD);
//    }
//
//    private Node.ContainerNode stepNode() {
//        return new ContainerNode("stepContainer", BYPASS_BEFORE_WHEN_BYPASS_MODE | DRY_AFTERS_ON_DRY | DRY_TARGETS_ON_DRY);
//    }
//
//    private Node.ContainerNode stepNodeErrorPolicy() {
//        return new ContainerNode("stepContainer", BYPASS_BEFORE_WHEN_BYPASS_MODE | DRY_AFTERS_ON_DRY | DRY_TARGETS_ON_DRY | DRY_CHILDES_ON_DRY |
//                BYPASS_CHILDREN_AFTER_STAGE_ERROR | DRY_BEFORES_ON_ERROR | DRY_TARGETS_ON_ERROR | DRY_AFTERS_ON_ERROR |
//                SWITCH_TO_DRY_FOR_BEFORE | SWITCH_TO_DRY_FOR_AFTER | SWITCH_TO_DRY_FOR_TARGET | SWITCH_TO_DRY_FOR_CHILD
//        );
//    }
}
