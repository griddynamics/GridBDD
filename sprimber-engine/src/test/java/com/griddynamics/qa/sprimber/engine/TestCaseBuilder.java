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

import static com.griddynamics.qa.sprimber.engine.TreeSuiteExecutor.Node.*;

/**
 * @author fparamonov
 */

@Slf4j
public class TestCaseBuilder {

    private final Method beforeMethod = ReflectionUtils.findMethod(TreeSuiteExecutor.class, "before");
    private final Method stepMethod = ReflectionUtils.findMethod(TreeSuiteExecutor.class, "step");
    private final Method afterMethod = ReflectionUtils.findMethod(TreeSuiteExecutor.class, "after");
    private final Method exceptionalStepMethod = ReflectionUtils.findMethod(TreeSuiteExecutor.class, "exceptionalStep");

    TreeSuiteExecutor.Node buildTestCase() {
        TreeSuiteExecutor.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        TreeSuiteExecutor.Node testNode = buildTestNodeWithOneSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    TreeSuiteExecutor.Node buildTestCaseWithTestWithTwoStep() {
        TreeSuiteExecutor.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        TreeSuiteExecutor.Node testNode = buildTestNodeWithTwoSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    TreeSuiteExecutor.Node buildTestCaseWithTestWithTwoStepDoubleHooks() {
        TreeSuiteExecutor.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        TreeSuiteExecutor.Node testNode = buildTestNodeWithTwoSurroundStepDoubleHooks();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    TreeSuiteExecutor.Node buildTestCaseWithException() {
        TreeSuiteExecutor.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        TreeSuiteExecutor.Node testNode = buildTestWithRegularAndExceptionalSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    TreeSuiteExecutor.Node buildTestCaseWithJustException() {
        TreeSuiteExecutor.ContainerNode testCaseNode = childNode();
        testCaseNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testCaseNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        TreeSuiteExecutor.Node testNode = buildTestWithJustExceptionalSurroundStep();
        testCaseNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(testNode);
        return testCaseNode;
    }

    TreeSuiteExecutor.Node buildTestNodeWithOneSurroundStep() {
        TreeSuiteExecutor.Node testNode = testNode();
        TreeSuiteExecutor.Node stepNode = buildStepSurroundWithSingleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(stepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    TreeSuiteExecutor.Node buildTestNodeWithTwoSurroundStep() {
        TreeSuiteExecutor.Node testNode = testNode();
        TreeSuiteExecutor.Node firstStepNode = buildStepSurroundWithSingleHooks();
        TreeSuiteExecutor.Node secondStepNode = buildStepSurroundWithSingleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    TreeSuiteExecutor.Node buildTestNodeWithTwoSurroundStepDoubleHooks() {
        TreeSuiteExecutor.Node testNode = testNode();
        TreeSuiteExecutor.Node firstStepNode = buildStepSurroundWithDoubleHooks();
        TreeSuiteExecutor.Node secondStepNode = buildStepSurroundWithDoubleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    TreeSuiteExecutor.Node buildTestWithRegularAndExceptionalSurroundStep() {
        TreeSuiteExecutor.Node testNode = testNode();
        TreeSuiteExecutor.Node firstStepNode = buildStepSurroundWithSingleHooks();
        TreeSuiteExecutor.Node secondStepNode = buildExceptionalStepSurroundWithSingleHooks();
        TreeSuiteExecutor.Node thirdStepNode = buildStepSurroundWithDoubleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(firstStepNode);
        testNode.getChildren().get("child").add(secondStepNode);
        testNode.getChildren().get("child").add(thirdStepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    TreeSuiteExecutor.Node buildTestWithJustExceptionalSurroundStep() {
        TreeSuiteExecutor.Node testNode = testNode();
        TreeSuiteExecutor.Node stepNode = buildExceptionalStepSurroundWithSingleHooks();
        testNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        testNode.getChildren().computeIfAbsent("child", k -> new ArrayList<>()).add(stepNode);
        testNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return testNode;
    }

    TreeSuiteExecutor.Node buildStepSurroundWithSingleHooks() {
        TreeSuiteExecutor.Node stepNode = stepNode();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(targetNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return stepNode;
    }

    TreeSuiteExecutor.Node buildExceptionalStepSurroundWithSingleHooks() {
        TreeSuiteExecutor.Node stepNode = stepNode();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(exceptionalStepNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        return stepNode;
    }

    TreeSuiteExecutor.Node buildStepSurroundWithDoubleHooks() {
        TreeSuiteExecutor.Node stepNode = stepNode();
        stepNode.getChildren().computeIfAbsent("before", k -> new ArrayList<>()).add(beforeNode());
        stepNode.getChildren().get("before").add(beforeNode());
        stepNode.getChildren().computeIfAbsent("target", k -> new ArrayList<>()).add(targetNode());
        stepNode.getChildren().computeIfAbsent("after", k -> new ArrayList<>()).add(afterNode());
        stepNode.getChildren().get("after").add(afterNode());
        return stepNode;
    }

    private TreeSuiteExecutor.ExecutableNode beforeNode() {
        TreeSuiteExecutor.ExecutableNode executableNode = new TreeSuiteExecutor.ExecutableNode();
        executableNode.setMethod(beforeMethod);
        return executableNode;
    }

    private TreeSuiteExecutor.ExecutableNode afterNode() {
        TreeSuiteExecutor.ExecutableNode executableNode = new TreeSuiteExecutor.ExecutableNode();
        executableNode.setMethod(afterMethod);
        return executableNode;
    }

    private TreeSuiteExecutor.ExecutableNode targetNode() {
        TreeSuiteExecutor.ExecutableNode executableNode = new TreeSuiteExecutor.ExecutableNode();
        executableNode.setMethod(stepMethod);
        return executableNode;
    }

    private TreeSuiteExecutor.ExecutableNode exceptionalStepNode() {
        TreeSuiteExecutor.ExecutableNode executableNode = new TreeSuiteExecutor.ExecutableNode();
        executableNode.setMethod(exceptionalStepMethod);
        return executableNode;
    }

    private TreeSuiteExecutor.ContainerNode testNode() {
        TreeSuiteExecutor.ContainerNode containerNode = new TreeSuiteExecutor.ContainerNode();
        containerNode.setSubNodesSkippingFlags(BEFORE_ON_SKIP | AFTER_ON_SKIP | TARGET_ON_SKIP | CHILD_ON_ERROR);
        return containerNode;
    }

    private TreeSuiteExecutor.ContainerNode stepNode() {
        TreeSuiteExecutor.ContainerNode containerNode = new TreeSuiteExecutor.ContainerNode();
        containerNode.setSubNodesSkippingFlags(BEFORE_ON_SKIP | AFTER_ON_SKIP | TARGET_ON_SKIP);
        return containerNode;
    }

    private TreeSuiteExecutor.ContainerNode childNode() {
        return new TreeSuiteExecutor.ContainerNode();
    }
}
