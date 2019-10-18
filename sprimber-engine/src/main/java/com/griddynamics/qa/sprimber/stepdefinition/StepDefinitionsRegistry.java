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

package com.griddynamics.qa.sprimber.stepdefinition;

import com.griddynamics.qa.sprimber.engine.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author fparamonov
 */
public class StepDefinitionsRegistry {

    /**
     * Collection that aimed to hold all available step definitions at runtime
     * Without binding to any suite/case/test/step
     */
    private final Map<String, StepDefinition> stepDefinitionsByKey = new HashMap<>();

    public void add(Map<String, StepDefinition> stepDefinitions) {
        stepDefinitionsByKey.putAll(stepDefinitions);
    }

    public Stream<StepDefinition> streamAllDefinitions() {
        return stepDefinitionsByKey.values().stream();
    }

    public Stream<Node> provideBeforeTestHookNodes() {
        return provideNodesForTargetTypeAndPhase(StepDefinition.StepType.BEFORE, StepDefinition.StepPhase.TEST);
    }

    public Stream<Node> provideAfterTestHookNodes() {
        return provideNodesForTargetTypeAndPhase(StepDefinition.StepType.AFTER, StepDefinition.StepPhase.TEST);
    }

    public Stream<Node> provideBeforeStepHookNodes() {
        return provideNodesForTargetTypeAndPhase(StepDefinition.StepType.BEFORE, StepDefinition.StepPhase.STEP);
    }

    public Stream<Node> provideAfterStepHookNodes() {
        return provideNodesForTargetTypeAndPhase(StepDefinition.StepType.AFTER, StepDefinition.StepPhase.STEP);
    }

    private Stream<Node> provideNodesForTargetTypeAndPhase(StepDefinition.StepType stepType, StepDefinition.StepPhase stepPhase) {
        return findStepHooksForCurrentStage(stepType, stepPhase)
                .map(this::provideExecutableNode);
    }

    private Stream<StepDefinition> findStepHooksForCurrentStage(StepDefinition.StepType stepType,
                                                                StepDefinition.StepPhase stepPhase) {
        return stepDefinitionsByKey.values().stream()
                .filter(stepDefinition -> stepDefinition.getStepType().equals(stepType))
                .filter(stepDefinition -> stepDefinition.getStepPhase().equals(stepPhase));
    }

    private Node provideExecutableNode(StepDefinition stepDefinition) {
        Node.ExecutableNode executableNode = new Node.ExecutableNode("step");
        executableNode.setName(stepDefinition.getName());
        executableNode.setMethod(stepDefinition.getMethod());
        executableNode.setName(stepDefinition.getMethod().getName());
        return executableNode;
    }
}
