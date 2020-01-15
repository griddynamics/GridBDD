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

import com.griddynamics.qa.sprimber.condition.PropertyCondition;
import com.griddynamics.qa.sprimber.condition.SkipOnProperty;
import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.stepdefinition.TestMethod;
import com.griddynamics.qa.sprimber.stepdefinition.TestMethodRegistry;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.Argument;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.griddynamics.qa.sprimber.engine.Node.*;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor
class PickleStepFactory {

    private static final String TAGS_ATTRIBUTE = "Tags";

    private final CucumberTagFilter tagFilter;
    private final StepMatcher stepMatcher;
    private final TestMethodRegistry testMethodRegistry;

    Node addStepContainerNode(Node parentNode, PickleStep stepCandidate) {
        Node.Builder builder = new Node.Builder()
                .withRole("stepContainer")
                .withSubNodeModes(BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_TARGET_WHEN_BYPASS_MODE);
        Node stepContainerNode = parentNode.addChild(builder);

        List<Node> testMethods = testMethodRegistry.streamAllTestMethods()
                .filter(testMethod -> StringUtils.isNotBlank(testMethod.getTextPattern()))
                .filter(filterTestMethodByTags())
                .map(testMethod ->
                        stepMatcher.matchAndGetArgumentsFrom(stepCandidate, testMethod, testMethod.getMethod().getParameterTypes())
                                .map(arguments -> addStepContainerNode(stepContainerNode, stepCandidate, testMethod, arguments))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (testMethods.size() == 0) {
            throw new StepNotFoundException(stepCandidate);
        }
        if (testMethods.size() > 1) {
            throw new ExtraMappingFoundException(testMethods, stepCandidate);
        }
        return stepContainerNode;
    }

    private Node addStepContainerNode(Node parentNode, PickleStep stepCandidate, TestMethod testMethod, List<Argument> arguments) {
        Node.Builder builder = new Node.Builder()
                .withRole("step")
                .withName(testMethod.getStyle() + " " + stepCandidate.getText())
                .withMethod(testMethod.getMethod())
                .withParameters(convertStepArguments(arguments, Arrays.asList(testMethod.getMethod().getGenericParameterTypes())));
        handleAdditionalStepData(stepCandidate)
                .ifPresent(stepData -> builder.withAttribute("stepData", stepData));
        buildCondition(testMethod.getMethod())
                .ifPresent(builder::withCondition);
        return parentNode.addTarget(builder);
    }

    Predicate<TestMethod> filterTestMethodByTags() {
        return testMethod ->
                tagFilter.filter((String) testMethod.getAttribute(TAGS_ATTRIBUTE));
    }

    private Optional<Condition> buildCondition(Method method) {
        PropertyCondition propertyCondition = null;
        if (method.isAnnotationPresent(SkipOnProperty.class)) {
            SkipOnProperty skipOnProperty = AnnotationUtils.getAnnotation(method, SkipOnProperty.class);
            propertyCondition = new PropertyCondition(skipOnProperty.value(), skipOnProperty.havingValue());
        }
        return Optional.ofNullable(propertyCondition);
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

        ExtraMappingFoundException(List<Node> nodeList, PickleStep pickleStep) {
            super(String.format("Step '%s' mapped to next methods '%s'!",
                    pickleStep.getText(),
                    nodeList.stream()
                            .map(Node::getMethod)
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
