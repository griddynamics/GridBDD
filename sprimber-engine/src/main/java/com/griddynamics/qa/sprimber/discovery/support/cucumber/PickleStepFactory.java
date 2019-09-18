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

package com.griddynamics.qa.sprimber.discovery.support.cucumber;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuite;
import com.griddynamics.qa.sprimber.discovery.support.AbstractBddStepFactory;
import com.griddynamics.qa.sprimber.engine.configuration.SprimberProperties;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author fparamonov
 */

@Component
@RequiredArgsConstructor
public class PickleStepFactory extends AbstractBddStepFactory<PickleStep> {

    private final SprimberProperties sprimberProperties;
    private final StepExpressionFactory stepExpressionFactory;
    private TagFilter tagFilter;

    @PostConstruct
    public void initTagFilter() {
        tagFilter = new TagFilter(sprimberProperties.getTagFilters());
    }

    @Override
    public TestSuite.Step provideStep(PickleStep stepCandidate) {
        List<StepDefinition> targetDefinitions = getStepDefinitions().values().stream()
                .filter(stepDefinition -> StringUtils.isNotBlank(stepDefinition.getBindingTextPattern()))
                .filter(stepDefinition -> Objects.nonNull(getArgumentsFromPickleStep(stepDefinition, stepCandidate)))
                .collect(Collectors.toList());
        if (targetDefinitions.size() == 0) {
            throw new StepNotFoundException(stepCandidate);
        }
        if (targetDefinitions.size() > 1) {
            throw new ExtraMappingFoundException(targetDefinitions, stepCandidate);
        }
        List<Argument> arguments = getArgumentsFromPickleStep(targetDefinitions.get(0), stepCandidate);
        List<Type> actualParameters = Arrays.asList(targetDefinitions.get(0).getMethod().getGenericParameterTypes());

        TestSuite.Step step = new TestSuite.Step();
        step.setStepDefinition(targetDefinitions.get(0));
        step.setName(stepCandidate.getText());
        step.getStepDefinition().getAttributes().put("stepData", handleAdditionalStepData(stepCandidate));
        step.getParameters().putAll(handleStepArguments(arguments, actualParameters));
        return step;
    }

    protected Predicate<StepDefinition> hooksByAvailableTags() {
        return stepDefinition ->
                tagFilter.filter((String) stepDefinition.getAttributes().get(CucumberStepDefinitionResolver.TAGS_ATTRIBUTE));
    }

    private Map<String, Object> handleStepArguments(List<Argument> arguments, List<Type> methodParameters) {
        return IntStream.range(0, methodParameters.size())
                .mapToObj(counter -> argumentToEntry(arguments.get(counter), methodParameters.get(counter)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, Object> argumentToEntry(Argument argument, Type type) {
        Object value = argument.getValue();
        if (value instanceof DataTable) {
            value = ((DataTable) value).convert(type, false);
        }
        return new ImmutablePair<>(type.getTypeName(), value);
    }

    /**
     * Will try to handle pickle table or return empty string if nothing to handle
     *
     * @param pickleStep - original pickle step from gherkin
     * @return - data table as string or empty
     */
    // TODO: 1/17/19 to add support for pickle string
    private String handleAdditionalStepData(PickleStep pickleStep) {
        return pickleStep.getArgument().stream()
                .filter(argument -> argument instanceof PickleTable)
                .findFirst()
                .map(table -> {
                            List<PickleRow> rows = ((PickleTable) table).getRows();
                            StringBuilder dataTableCsv = new StringBuilder();

                            if (!rows.isEmpty()) {
                                rows.forEach(dataTableRow -> {
                                    dataTableCsv.append(
                                            dataTableRow.getCells().stream()
                                                    .map(PickleCell::getValue)
                                                    .collect(Collectors.joining("\t"))
                                    );
                                    dataTableCsv.append('\n');
                                });
                            }
                            return dataTableCsv.toString();
                        }
                ).orElse("");
    }

    private List<Argument> getArgumentsFromPickleStep(StepDefinition definition, PickleStep pickleStep) {
        StepExpression stepExpression = stepExpressionFactory.createExpression(definition.getBindingTextPattern());
        ExpressionArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(stepExpression);
        return argumentMatcher.argumentsFrom(pickleStep);
    }

    class ExtraMappingFoundException extends RuntimeException {

        ExtraMappingFoundException(List<StepDefinition> stepDefinitions, PickleStep pickleStep) {
            super(String.format("Step '%s' mapped to next methods '%s'!",
                    pickleStep.getText(),
                    stepDefinitions.stream()
                            .map(StepDefinition::getMethod)
                            .map(Method::toString)
                            .collect(Collectors.joining(","))
            ));
        }
    }

    class StepNotFoundException extends RuntimeException {

        StepNotFoundException(PickleStep pickleStep) {
            super(String.format("Step '%s' not found!", pickleStep.getText()));
        }
    }

}
