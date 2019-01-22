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

import com.griddynamics.qa.sprimber.engine.model.TestStep;
import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.model.action.ActionsContainer;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author fparamonov
 */

@Component
public class PickleStepProcessor {

    private final ActionsContainer actionsContainer;
    private final StepExpressionFactory stepExpressionFactory;

    public PickleStepProcessor(ActionsContainer actionsContainer,
                               StepExpressionFactory stepExpressionFactory) {
        this.actionsContainer = actionsContainer;
        this.stepExpressionFactory = stepExpressionFactory;
    }

    public TestStep processPickleStep(PickleStep pickleStep) {
        List<ActionDefinition> targetDefinitions = actionsContainer.getDefinitions().stream()
                .filter(definition -> definition.getActionText().isPresent())
                .filter(definition -> Objects.nonNull(getArgumentsFromPickleStep(definition, pickleStep)))
                .collect(Collectors.toList());
        if (targetDefinitions.size() == 0) {
            throw new StepNotFoundException(pickleStep);
        }
        if (targetDefinitions.size() > 1) {
            throw new ExtraMappingFoundException(targetDefinitions, pickleStep);
        }
        List<Argument> arguments = getArgumentsFromPickleStep(targetDefinitions.get(0), pickleStep);
        List<Type> actualParameters = Arrays.asList(targetDefinitions.get(0).getMethod().getGenericParameterTypes());

        TestStep testStep = new TestStep();
        testStep.setActualText(pickleStep.getText());
        testStep.setStepDataAsText(handleAdditionalStepData(pickleStep));
        testStep.setStepAction(targetDefinitions.get(0));
        testStep.setStepArguments(processArguments(arguments, actualParameters));
        return testStep;
    }

    private Object[] processArguments(List<Argument> arguments, List<Type> methodParameters) {
        return IntStream.range(0, methodParameters.size())
                .mapToObj(counter -> argumentToObject(arguments.get(counter), methodParameters.get(counter)))
                .toArray();
    }

    private Object argumentToObject(Argument argument, Type type) {
        Object value = argument.getValue();
        if (value instanceof DataTable) {
            value = ((DataTable) value).convert(type, false);
        }
        return value;
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

    private List<Argument> getArgumentsFromPickleStep(ActionDefinition definition, PickleStep pickleStep) {
        StepExpression stepExpression = stepExpressionFactory.createExpression(definition.getActionText().orElse(""));
        ExpressionArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(stepExpression);
        return argumentMatcher.argumentsFrom(pickleStep);
    }
}
