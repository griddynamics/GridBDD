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

package com.griddynamics.qa.sprimber.engine.processor.cucumber;

import com.griddynamics.qa.sprimber.engine.model.TestCase;
import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.model.action.ActionScope;
import com.griddynamics.qa.sprimber.engine.model.action.ActionType;
import com.griddynamics.qa.sprimber.engine.model.action.ActionsContainer;
import com.griddynamics.qa.sprimber.engine.model.action.details.CucumberHookDetails;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Component
public class PickleProcessor {

    private final ActionsContainer actionsContainer;
    private final PickleStepProcessor pickleStepProcessor;

    public PickleProcessor(ActionsContainer actionsContainer,
                           PickleStepProcessor pickleStepProcessor) {
        this.actionsContainer = actionsContainer;
        this.pickleStepProcessor = pickleStepProcessor;
    }

    public TestCase processPickle(CucumberDocument cucumberDocument, Pickle pickle) {
        TestCase testCase = new TestCase();
        List<String> tags = getTagsForPickle(pickle);
        testCase.getAllHooks().addAll(
                actionsContainer.getDefinitions().stream()
                        .filter(testCaseScopePredicate())
                        .filter(testCaseHookTypePredicate())
                        .filter(hookTagPredicate(tags))
                        .collect(Collectors.toList())
        );
        testCase.getSteps().addAll(
                pickle.getSteps().stream()
                        .map(pickleStepProcessor::processPickleStep)
                        .collect(Collectors.toList())
        );

        testCase.getTags().addAll(tags);
        testCase.setName(pickle.getName());
        testCase.setParentName(cucumberDocument.getDocument().getFeature().getName());
        testCase.setDescription(cucumberDocument.getDocument().getFeature().getDescription());
        testCase.setUrl(cucumberDocument.getUrl().getPath());
        testCase.setRuntimeId(UUID.randomUUID().toString());
        return testCase;
    }

    private Predicate<ActionDefinition> testCaseScopePredicate() {
        return actionDefinition ->
                ActionScope.SCENARIO.equals(actionDefinition.getActionScope()) || ActionScope.STEP.equals(actionDefinition.getActionScope());
    }

    private Predicate<ActionDefinition> testCaseHookTypePredicate() {
        return actionDefinition ->
                ActionType.Before.equals(actionDefinition.getActionType()) ||
                        ActionType.After.equals(actionDefinition.getActionType());
    }

    private Predicate<ActionDefinition> hookTagPredicate(List<String> tags) {
        return definition -> {
            TagFilter tagFilter = new TagFilter(((CucumberHookDetails) definition.getMetaInfo()).getValues());
            return tagFilter.filter(tags);
        };
    }

    private List<String> getTagsForPickle(Pickle pickle) {
        return pickle.getTags().stream()
                .map(PickleTag::getName)
                .collect(Collectors.toList());
    }
}
