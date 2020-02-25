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

import static com.griddynamics.qa.sprimber.engine.Node.SkipOptions.BYPASS_WHEN_STAGE_HAS_ERROR;
import static com.griddynamics.qa.sprimber.engine.Node.SkipOptions.BYPASS_WHEN_SUB_STAGE_HAS_ERROR;

/**
 * @author fparamonov
 */

@Slf4j
class TestCaseBuilder {

    private static final String TEST_ADAPTER_NAME = "testAdapter";
    private final Method beforeMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "before");
    private final Method stepMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "step");
    private final Method afterMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "after");
    private final Method exceptionalStepMethod = ReflectionUtils.findMethod(StubbedNodeInvoker.class, "exceptionalStep");

    Node buildSingleStep() {
        Node.Builder builder = new Node.Builder()
                .withRole("stepRoot")
                .withAdapterName(TEST_ADAPTER_NAME)
                .withBeforeBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withTargetBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withAfterBypassOptions(EnumSet.noneOf(Node.SkipOptions.class));
        Node stepNode = Node.createRootNode(builder);
        stepNode.addInvokableTarget(getStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStep() {
        Node.Builder builder = new Node.Builder()
                .withRole("stepRoot")
                .withAdapterName(TEST_ADAPTER_NAME)
                .withBeforeBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withTargetBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withAfterBypassOptions(EnumSet.noneOf(Node.SkipOptions.class));
        Node stepNode = Node.createRootNode(builder);
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableTarget(getStepBuilder());
        stepNode.addInvokableAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildTestWithRegularAndExceptionalWrappedStep() {
        Node.Builder testBuilder = new Node.Builder()
                .withRole("testRoot")
                .withAdapterName(TEST_ADAPTER_NAME)
                .withBeforeBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withTargetBypassOptions(EnumSet.of(BYPASS_WHEN_STAGE_HAS_ERROR))
                .withAfterBypassOptions(EnumSet.noneOf(Node.SkipOptions.class));
        Node testNode = Node.createRootNode(testBuilder);
        Node.Builder exceptionalBuilder = new Node.Builder()
                .withRole("stepRoot")
                .withBeforeBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withTargetBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withAfterBypassOptions(EnumSet.noneOf(Node.SkipOptions.class));
        Node exceptionalStepNode = testNode.addContainerTarget(exceptionalBuilder);
        exceptionalStepNode.addInvokableBefore(getBeforeStepBuilder());
        exceptionalStepNode.addInvokableTarget(getExceptionalStepBuilder("step"));
        exceptionalStepNode.addInvokableAfter(getAfterStepBuilder());
        Node.Builder regularBuilder = new Node.Builder()
                .withRole("stepRoot")
                .withBeforeBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withTargetBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withAfterBypassOptions(EnumSet.noneOf(Node.SkipOptions.class));
        Node regularStepNode = testNode.addContainerTarget(regularBuilder);
        regularStepNode.addInvokableBefore(getBeforeStepBuilder());
        regularStepNode.addInvokableTarget(getStepBuilder());
        regularStepNode.addInvokableAfter(getAfterStepBuilder());
        return testNode;
    }

    Node buildSingleWrappedStepWithExceptionalBefore() {
        Node.Builder builder = new Node.Builder()
                .withRole("stepRoot")
                .withAdapterName(TEST_ADAPTER_NAME)
                .withBeforeBypassOptions(EnumSet.of(BYPASS_WHEN_SUB_STAGE_HAS_ERROR))
                .withTargetBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withAfterBypassOptions(EnumSet.noneOf(Node.SkipOptions.class));
        Node stepNode = Node.createRootNode(builder);
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableBefore(getExceptionalStepBuilder("before"));
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableTarget(getStepBuilder());
        stepNode.addInvokableAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTarget() {
        Node.Builder builder = new Node.Builder()
                .withRole("stepRoot")
                .withAdapterName(TEST_ADAPTER_NAME)
                .withBeforeBypassOptions(EnumSet.of(BYPASS_WHEN_SUB_STAGE_HAS_ERROR))
                .withTargetBypassOptions(EnumSet.of(BYPASS_WHEN_STAGE_HAS_ERROR))
                .withAfterBypassOptions(EnumSet.noneOf(Node.SkipOptions.class));
        Node stepNode = Node.createRootNode(builder);
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableBefore(getExceptionalStepBuilder("before"));
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableTarget(getStepBuilder());
        stepNode.addInvokableAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTargetAndAfter() {
        Node.Builder builder = new Node.Builder()
                .withRole("stepRoot")
                .withAdapterName(TEST_ADAPTER_NAME)
                .withBeforeBypassOptions(EnumSet.allOf(Node.SkipOptions.class))
                .withTargetBypassOptions(EnumSet.of(BYPASS_WHEN_STAGE_HAS_ERROR))
                .withAfterBypassOptions(EnumSet.of(Node.SkipOptions.BYPASS_WHEN_STAGE_HAS_ERROR));
        Node stepNode = Node.createRootNode(builder);
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableBefore(getExceptionalStepBuilder("before"));
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableTarget(getStepBuilder());
        stepNode.addInvokableAfter(getAfterStepBuilder());
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalAfter() {
        Node.Builder builder = new Node.Builder()
                .withRole("stepRoot")
                .withAdapterName(TEST_ADAPTER_NAME)
                .withBeforeBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withTargetBypassOptions(EnumSet.noneOf(Node.SkipOptions.class))
                .withAfterBypassOptions(EnumSet.of(BYPASS_WHEN_SUB_STAGE_HAS_ERROR));
        Node stepNode = Node.createRootNode(builder);
        stepNode.addInvokableBefore(getBeforeStepBuilder());
        stepNode.addInvokableTarget(getStepBuilder());
        stepNode.addInvokableAfter(getAfterStepBuilder());
        stepNode.addInvokableAfter(getExceptionalStepBuilder("after"));
        stepNode.addInvokableAfter(getAfterStepBuilder());
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
