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

package com.griddynamics.qa.sprimber.engine.model;

import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.model.action.ActionScope;
import com.griddynamics.qa.sprimber.engine.model.action.ActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central abstraction on top of BDD scenario. This one class that hold all information that related to each scenario
 * like hooks, features, methods and so on.
 *
 * @author fparamonov
 */
public class TestCase {

    private List<ActionDefinition> allHooks = new ArrayList<>();
    private List<TestStep> steps = new ArrayList<>();
    // TODO: 12/06/2018 If this changes not enough need to add more meta details

    private List<String> tags = new ArrayList<>();

    /**
     * Equivalent to scenario name/description
     */
    private String name;
    /**
     * Equivalent to feature/story name
     */
    private String parentName;

    /**
     * Equivalent to feature/story description
     */
    private String description;

    /**
     * Each scenario tagged with unique id for current execution
     * The value can be tracked by core of Sprimber but initialization must happen
     * at the bridge implementation side.
     */
    private String runtimeId;


    public List<ActionDefinition> getBeforeScenarioActions() {
        return allHooks.stream()
                .filter(definition ->
                        ActionScope.SCENARIO.equals(definition.getActionScope()) && ActionType.Before.equals(definition.getActionType()))
                .collect(Collectors.toList());
    }

    public List<ActionDefinition> getBeforeStepActions() {
        return allHooks.stream()
                .filter(definition ->
                        ActionScope.STEP.equals(definition.getActionScope()) && ActionType.Before.equals(definition.getActionType()))
                .collect(Collectors.toList());
    }

    public List<ActionDefinition> getAfterStepActions() {
        return allHooks.stream()
                .filter(definition ->
                        ActionScope.STEP.equals(definition.getActionScope()) && ActionType.After.equals(definition.getActionType()))
                .collect(Collectors.toList());
    }

    public List<ActionDefinition> getAfterScenarioActions() {
        return allHooks.stream()
                .filter(definition ->
                        ActionScope.SCENARIO.equals(definition.getActionScope()) && ActionType.After.equals(definition.getActionType()))
                .collect(Collectors.toList());
    }

    public List<ActionDefinition> getAllHooks() {
        return allHooks;
    }

    public List<TestStep> getSteps() {
        return steps;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
    }
}
