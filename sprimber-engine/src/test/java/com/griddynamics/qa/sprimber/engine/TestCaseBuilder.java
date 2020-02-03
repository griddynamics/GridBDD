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

import static com.griddynamics.qa.sprimber.engine.Node.Builder;
import static com.griddynamics.qa.sprimber.engine.Node.Bypass.*;

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
        Node stepNode = Node.createRootNode("stepRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        stepNode.addTarget("step", stepMethod);
        return stepNode;
    }

    Node buildSingleWrappedStep() {
        Node stepNode = Node.createRootNode("stepRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildTestWithRegularAndExceptionalWrappedStep() {
        Node testNode = Node.createRootNode("testRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_CHILDREN_AFTER_ITERATION_ERROR));
        Node exceptionalStepNode = testNode.addChild("stepHolder", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        exceptionalStepNode.addBefore("before", beforeMethod);
        exceptionalStepNode.addTarget("step", exceptionalStepMethod);
        exceptionalStepNode.addAfter("after", afterMethod);
        Node regularStepNode = testNode.addChild("stepHolder", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        regularStepNode.addBefore("before", beforeMethod);
        regularStepNode.addTarget("step", stepMethod);
        regularStepNode.addAfter("after", afterMethod);
        return testNode;
    }

    Node buildSingleWrappedStepWithExceptionalBefore() {
        Node stepNode = Node.createRootNode("stepRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_BEFORE_AFTER_ITERATION_ERROR));
        stepNode.addBefore("before", beforeMethod);
        stepNode.addBefore("before", exceptionalStepMethod);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalAfter() {
        Node stepNode = Node.createRootNode("stepRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_AFTER_AFTER_ITERATION_ERROR));
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        stepNode.addAfter("after", exceptionalStepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTarget() {
        Node stepNode = Node.createRootNode("stepRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE, BYPASS_AFTER_WHEN_BYPASS_MODE,
                BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_BEFORE_AFTER_ITERATION_ERROR, BYPASS_TARGET_AFTER_STAGE_ERROR));
        stepNode.addBefore("before", beforeMethod);
        stepNode.addBefore("before", exceptionalStepMethod);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildSingleWrappedStepWithExceptionalBeforeSkipTargetAndAfter() {
        Node stepNode = Node.createRootNode("stepRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE, BYPASS_AFTER_WHEN_BYPASS_MODE,
                BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_BEFORE_AFTER_ITERATION_ERROR, BYPASS_TARGET_AFTER_STAGE_ERROR, BYPASS_AFTER_AFTER_STAGE_ERROR));
        stepNode.addBefore("before", beforeMethod);
        stepNode.addBefore("before", exceptionalStepMethod);
        stepNode.addBefore("before", beforeMethod);
        stepNode.addTarget("step", stepMethod);
        stepNode.addAfter("after", afterMethod);
        return stepNode;
    }

    Node buildAsyncAndRegularWrappedSteps() {
        Node testRoot = Node.createRootNode("testRoot", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE, BYPASS_CHILDREN_AFTER_ITERATION_ERROR));

        Node asyncStepHolder = testRoot.addChild("stepHolder", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        asyncStepHolder.addBefore("before", beforeMethod);
        Builder builder = new Builder()
                .withRole("target")
                .withMethod(stepMethod)
                .withAsyncEnabled("testRoot");
        asyncStepHolder.addTarget(builder);
        asyncStepHolder.addAfter("after", afterMethod);

        Node stepHolderNode = testRoot.addChild("stepHolder", EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_TARGET_WHEN_BYPASS_MODE));
        stepHolderNode.addBefore("before", beforeMethod);
        stepHolderNode.addTarget("step", stepMethod);
        stepHolderNode.addAfter("after", afterMethod);
        return testRoot;
    }
}
