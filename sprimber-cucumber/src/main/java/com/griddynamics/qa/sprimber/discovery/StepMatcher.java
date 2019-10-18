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

import com.griddynamics.qa.sprimber.stepdefinition.StepDefinition;
import gherkin.pickles.*;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Extended version of {@link io.cucumber.stepexpression.ArgumentMatcher} with ability
 * to reuse step expressions for once building step expressions
 *
 * @author fparamonov
 */

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class StepMatcher {

    private final StepExpressionFactory stepExpressionFactory;
    private final Map<String, StepExpression> stepExpressionsByBindingPattern = new HashMap<>();

    /**
     * The implementation follows the default Cucumber implementation - there is no distinct boolean match method
     * To identify that pattern match with step definition only one way exist - to get arguments
     * If arguments present(not null) then step match the pattern
     *
     * @param pickleStep - candidate for match
     * @param stepDefinition - holder for pattern
     * @param types - target arguments parameters
     * @return - optional of arguments - that mean once present then match.
     */
    Optional<List<Argument>> matchAndGetArgumentsFrom(PickleStep pickleStep, StepDefinition stepDefinition, Type... types) {
        StepExpression stepExpression = stepExpressionsByBindingPattern
                .computeIfAbsent(stepDefinition.getBindingTextPattern(), stepExpressionFactory::createExpression);

        if (pickleStep.getArgument().isEmpty()) {
            return Optional.ofNullable(stepExpression.match(pickleStep.getText(), types));
        }

        gherkin.pickles.Argument argument = pickleStep.getArgument().get(0);

        if (argument instanceof PickleString) {
            return Optional.ofNullable(stepExpression.match(pickleStep.getText(), ((PickleString) argument).getContent(), types));
        }

        if (argument instanceof PickleTable) {
            return Optional.ofNullable(stepExpression.match(pickleStep.getText(), toTable((PickleTable) argument), types));
        }

        throw new IllegalStateException("Argument was neither PickleString nor PickleTable");
    }

    /**
     * Reuse from PickleTableTransformer that has package access
     * @param pickleTable
     * @return
     */
    private List<List<String>> toTable(PickleTable pickleTable) {
        List<List<String>> table = new ArrayList<>();
        for (PickleRow pickleRow : pickleTable.getRows()) {
            List<String> row = new ArrayList<>();
            for (PickleCell pickleCell : pickleRow.getCells()) {
                row.add(pickleCell.getValue());
            }
            table.add(row);
        }
        return table;
    }
}
