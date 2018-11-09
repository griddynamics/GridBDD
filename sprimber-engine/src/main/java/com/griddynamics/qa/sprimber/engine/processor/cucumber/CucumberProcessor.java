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
import com.griddynamics.qa.sprimber.engine.model.TestStep;
import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.model.action.ActionType;
import com.griddynamics.qa.sprimber.engine.model.action.ActionsContainer;
import com.griddynamics.qa.sprimber.engine.model.action.details.CucumberHookDetails;
import com.griddynamics.qa.sprimber.engine.model.configuration.SprimberProperties;
import com.griddynamics.qa.sprimber.engine.processor.ResourceProcessor;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This implementation of common resource processor interface aimed to resolve Gherkin based features
 * to Sprimber BDD Java abstraction
 *
 * @author fparamonov
 */

@Component
public class CucumberProcessor implements ResourceProcessor {

    private final Parser<GherkinDocument> gherkinParser;
    private final TokenMatcher tokenMatcher;
    private final Compiler compiler;
    private final SprimberProperties sprimberProperties;
    private final ActionsContainer actionsContainer;
    private final StepExpressionFactory stepExpressionFactory;
    private final ApplicationContext applicationContext;

    public CucumberProcessor(Parser<GherkinDocument> gherkinParser,
                             TokenMatcher tokenMatcher,
                             Compiler compiler,
                             SprimberProperties sprimberProperties,
                             ActionsContainer actionsContainer,
                             StepExpressionFactory stepExpressionFactory,
                             ApplicationContext applicationContext) {
        this.gherkinParser = gherkinParser;
        this.tokenMatcher = tokenMatcher;
        this.compiler = compiler;
        this.sprimberProperties = sprimberProperties;
        this.actionsContainer = actionsContainer;
        this.stepExpressionFactory = stepExpressionFactory;
        this.applicationContext = applicationContext;
    }

    @Override
    public List<TestCase> process() {
        List<TestCase> testCases = new ArrayList<>();
        try {
            testCases = Arrays.stream(applicationContext.getResources(sprimberProperties.getFeaturePath()))
                    .map(this::buildGherkinDocument)
                    .flatMap(this::bundlePickleAndFeature)
                    .filter(testCaseTagPredicate())
                    .map(this::processPickle)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testCases;
    }

    private TestCase processPickle(TestCaseMetaInfo testCaseMetaInfo) {
        TestCase testCase = new TestCase();
        Pickle pickle = testCaseMetaInfo.getPickle();
        testCase.getBeforeActions().addAll(
                findDefinitionsByType(ActionType.Before).stream()
                        .filter(hookTagPredicate(pickle))
                        .collect(Collectors.toList()));
        testCase.getSteps().addAll(pickle.getSteps().stream().map(this::processPickleStep).collect(Collectors.toList()));
        testCase.getAfterActions().addAll(
                findDefinitionsByType(ActionType.After).stream()
                        .filter(hookTagPredicate(pickle))
                        .collect(Collectors.toList()));

        testCase.setName(pickle.getName());
        testCase.setParentName(testCaseMetaInfo.getFeature().getName());
        testCase.setDescription(testCaseMetaInfo.getFeature().getDescription());
        testCase.setRuntimeId(UUID.randomUUID().toString());
        testCase.getTags().addAll(pickle.getTags().stream().map(PickleTag::getName).collect(Collectors.toList()));
        return testCase;
    }

    private TestStep processPickleStep(PickleStep pickleStep) {
        TestStep testStep = new TestStep();
        testStep.setActualText(pickleStep.getText());
        testStep.getBeforeStepActions().addAll(findDefinitionsByType(ActionType.BeforeStep));

        ActionDefinitionAndArguments pair = findDefinitionAndArgumentsByActualStep(pickleStep);
        testStep.setStepAction(pair.getDefinition());
        testStep.setStepArguments(processArguments(pair));
        testStep.getAfterStepActions().addAll(findDefinitionsByType(ActionType.AfterStep));
        return testStep;
    }

    private Object[] processArguments(ActionDefinitionAndArguments pair) {
        List<Argument> arguments = pair.getArguments();
        List<Type> parameters = Arrays.asList(pair.getDefinition().getMethod().getGenericParameterTypes());
        return IntStream.range(0, parameters.size())
                .mapToObj(counter -> processArgument(arguments.get(counter), parameters.get(counter))).toArray();
    }

    private Object processArgument(Argument argument, Type type) {
        Object value = argument.getValue();
        if (value instanceof DataTable) {
            value = ((DataTable) value).convert(type, false);
        }
        return value;
    }

    private GherkinDocument buildGherkinDocument(Resource resource) {
        try {
            return gherkinParser.parse(new InputStreamReader(resource.getInputStream()), tokenMatcher);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<TestCaseMetaInfo> bundlePickleAndFeature(GherkinDocument document) {
        return compiler.compile(document).stream().map(pickle -> {
                    TestCaseMetaInfo testCaseMetaInfo = new TestCaseMetaInfo();
                    testCaseMetaInfo.setFeature(document.getFeature());
                    testCaseMetaInfo.setPickle(pickle);
                    return testCaseMetaInfo;
                }
        );
    }

    private ActionDefinitionAndArguments findDefinitionAndArgumentsByActualStep(PickleStep pickleStep) {
        return actionsContainer.getDefinitions().stream()
                .filter(definition -> Objects.nonNull(definition.getActionText()))
                .map(definition -> buildPairFromDefinitionAndStep(definition, pickleStep))
                .filter(pair -> Objects.nonNull(pair.getArguments()))
                .findFirst().orElseThrow(() -> new StepNotFoundException(pickleStep));
        // TODO: 09/06/2018 Need to handle case when step not found and handle case when more then one steps found
    }

    private ActionDefinitionAndArguments buildPairFromDefinitionAndStep(ActionDefinition definition, PickleStep pickleStep) {
        StepExpression stepExpression = stepExpressionFactory.createExpression(definition.getActionText());
        ExpressionArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(stepExpression);
        ActionDefinitionAndArguments actionDefinitionAndArguments = new ActionDefinitionAndArguments();
        actionDefinitionAndArguments.setDefinition(definition);
        actionDefinitionAndArguments.setArguments(argumentMatcher.argumentsFrom(pickleStep));
        return actionDefinitionAndArguments;
    }

    private List<ActionDefinition> findDefinitionsByType(ActionType actionType) {
        return actionsContainer.getDefinitions().stream()
                .filter(definition -> actionType.equals(definition.getActionType()))
                .collect(Collectors.toList());
    }

    private Predicate<TestCaseMetaInfo> testCaseTagPredicate() {
        return testCaseMetaInfo -> {
            TagFilter tagFilter = new TagFilter(sprimberProperties.getTagFilters());
            return tagFilter.filter(testCaseMetaInfo.getPickle());
        };
    }

    private Predicate<ActionDefinition> hookTagPredicate(Pickle pickle) {
        return definition -> {
            TagFilter tagFilter = new TagFilter(((CucumberHookDetails) definition.getMetaInfo()).getValues());
            return tagFilter.filter(pickle);
        };
    }
}
