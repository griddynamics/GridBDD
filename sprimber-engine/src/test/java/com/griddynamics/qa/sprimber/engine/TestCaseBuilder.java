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
import java.util.EnumSet;

import static com.griddynamics.qa.sprimber.engine.Node.Bypass.*;

/**
 * @author fparamonov
 */

@Slf4j
public class TestCaseBuilder {

    private static final String TEST_ADAPTER_NAME = "testAdapter";
    private final Method beforeMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "before");
    private final Method stepMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "step");
    private final Method afterMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "after");
    private final Method exceptionalStepMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "exceptionalStep");

    Node buildSingleStep() {
        Node stepNode = Node.createRootNode("stepRoot", TEST_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        stepNode.addTarget(getStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStep() {
        Node stepNode = Node.createRootNode("stepRoot", TEST_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addTarget(getStepBuilder());
        stepNode.addAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildTestWithRegularAndExceptionalWrappedStep() {
        Node testNode = Node.createRootNode("testRoot", TEST_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_CHILDREN_AFTER_ITERATION_ERROR));
        Node.Builder stepHolderBuilder = new Node.Builder()
                .withRole("stepHolder")
                .withSubNodeModes(EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                        BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        Node exceptionalStepNode = testNode.addChild(stepHolderBuilder);
        exceptionalStepNode.addBefore(getBeforeStepBuilder());
        exceptionalStepNode.addTarget(getExceptionalStepBuilder("step"));
        exceptionalStepNode.addAfter(getAfterStepBuilder());
        Node regularStepNode = testNode.addChild(stepHolderBuilder);
        regularStepNode.addBefore(getBeforeStepBuilder());
        regularStepNode.addTarget(getStepBuilder());
        regularStepNode.addAfter(getAfterStepBuilder());
        return testNode;
    }

    Node buildSingleWrappedStepWithExceptionalBefore() {
        Node stepNode = Node.createRootNode("stepRoot", TEST_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_BEFORE_AFTER_ITERATION_ERROR));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addBefore(getExceptionalStepBuilder("before"));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addTarget(getStepBuilder());
        stepNode.addAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalAfter() {
        Node stepNode = Node.createRootNode("stepRoot", TEST_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_AFTER_AFTER_ITERATION_ERROR));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addTarget(getStepBuilder());
        stepNode.addAfter(getAfterStepBuilder());
        stepNode.addAfter(getExceptionalStepBuilder("after"));
        stepNode.addAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTarget() {
        Node stepNode = Node.createRootNode("stepRoot", TEST_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE, BYPASS_AFTER_WHEN_BYPASS_MODE,
                BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_BEFORE_AFTER_ITERATION_ERROR, BYPASS_TARGET_AFTER_STAGE_ERROR));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addBefore(getExceptionalStepBuilder("before"));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addTarget(getStepBuilder());
        stepNode.addAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTargetAndAfter() {
        Node stepNode = Node.createRootNode("stepRoot", TEST_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE, BYPASS_AFTER_WHEN_BYPASS_MODE,
                BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_BEFORE_AFTER_ITERATION_ERROR, BYPASS_TARGET_AFTER_STAGE_ERROR, BYPASS_AFTER_AFTER_STAGE_ERROR));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addBefore(getExceptionalStepBuilder("before"));
        stepNode.addBefore(getBeforeStepBuilder());
        stepNode.addTarget(getStepBuilder());
        stepNode.addAfter(getAfterStepBuilder());
        return stepNode;
    }

    private Node.Builder getBeforeStepBuilder() {
        return new Node.Builder()
                .withRole("before")
                .withMethod(beforeMethod);
    }

    private Node.Builder getStepBuilder() {
        return new Node.Builder()
                .withRole("step")
                .withMethod(stepMethod);
    }

    private Node.Builder getAfterStepBuilder() {
        return new Node.Builder()
                .withRole("after")
                .withMethod(afterMethod);
    }

    private Node.Builder getExceptionalStepBuilder(String role) {
        return new Node.Builder()
                .withRole(role)
                .withMethod(exceptionalStepMethod);
    }
}
