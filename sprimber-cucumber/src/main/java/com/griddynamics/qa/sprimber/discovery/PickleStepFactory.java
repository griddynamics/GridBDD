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

package com.griddynamics.qa.sprimber.discovery;

import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.stepdefinition.StepDefinition;
import com.griddynamics.qa.sprimber.stepdefinition.StepDefinitionsRegistry;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.Argument;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor
class PickleStepFactory implements StepFactory<PickleStep> {

    private static final String TAGS_ATTRIBUTE = "Tags";

    private final TagFilter tagFilter;
    private final StepMatcher stepMatcher;
    private final StepDefinitionsRegistry stepDefinitionsRegistry;

    public Node provideStepNode(PickleStep stepCandidate) {
        List<Node.ExecutableNode> steps = stepDefinitionsRegistry.streamAllDefinitions()
                .filter(stepDefinition -> StringUtils.isNotBlank(stepDefinition.getBindingTextPattern()))
                .map(stepDefinition ->
                        stepMatcher.matchAndGetArgumentsFrom(stepCandidate, stepDefinition, stepDefinition.getMethod().getParameterTypes())
                                .map(arguments -> buildStepNode(stepCandidate, stepDefinition, arguments))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (steps.size() == 0) {
            throw new StepNotFoundException(stepCandidate);
        }
        if (steps.size() > 1) {
            throw new ExtraMappingFoundException(steps, stepCandidate);
        }
        return steps.get(0);
    }

    private Node.ExecutableNode buildStepNode(PickleStep stepCandidate, StepDefinition stepDefinition, List<Argument> arguments) {
        Node.ExecutableNode executableStepNode = new Node.ExecutableNode("step");
        executableStepNode.setMethod(stepDefinition.getMethod());
        executableStepNode.setName(stepDefinition.getStepType() + " " + stepCandidate.getText());
        handleAdditionalStepData(stepCandidate)
                .ifPresent(stepData -> executableStepNode.getAttributes().put("stepData", stepData));
        executableStepNode.getParameters().putAll(convertStepArguments(arguments, Arrays.asList(stepDefinition.getMethod().getGenericParameterTypes())));
        return executableStepNode;
    }

    Predicate<Node> filterNodeByTags() {
        return node ->
                tagFilter.filter((String) node.getAttributes().get(TAGS_ATTRIBUTE));
    }

    private Map<String, Object> convertStepArguments(List<Argument> arguments, List<Type> methodParameters) {
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
    private Optional<String> handleAdditionalStepData(PickleStep pickleStep) {
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
                );
    }

    class ExtraMappingFoundException extends RuntimeException {

        ExtraMappingFoundException(List<Node.ExecutableNode> executableNodes, PickleStep pickleStep) {
            super(String.format("Step '%s' mapped to next methods '%s'!",
                    pickleStep.getText(),
                    executableNodes.stream()
                            .map(Node.ExecutableNode::getMethod)
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
