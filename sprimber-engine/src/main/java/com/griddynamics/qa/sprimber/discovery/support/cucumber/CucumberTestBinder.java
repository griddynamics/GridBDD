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
import com.griddynamics.qa.sprimber.discovery.support.TestSuiteDiscovery;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Component
@RequiredArgsConstructor
public class CucumberTestBinder implements TestSuiteDiscovery.TestDefinitionBinder<Pickle> {

    public static final String BDD_TAGS_ATTRIBUTE_NAME = "bddTags";
    public static final String LOCATION_ATTRIBUTE_NAME = "location";

    private final PickleStepFactory pickleStepFactory;
    private final StandardFallbackStrategy fallbackStrategy = new StandardFallbackStrategy();

    @Override
    public TestSuite.Test bind(Pickle testCandidate) {
        val testDefinition = new TestSuite.Test();
        testDefinition.getAttributes().put(BDD_TAGS_ATTRIBUTE_NAME, getTagsFromPickle(testCandidate));
        testDefinition.getAttributes().put(LOCATION_ATTRIBUTE_NAME, formatLocation(testCandidate));
        testDefinition.setName(testCandidate.getName());
        testDefinition.getSteps().addAll(pickleStepFactory.provideBeforeTestHooks());
        testCandidate.getSteps().stream()
                .map(pickleStepFactory::provideStep)
                .map(pickleStepFactory::wrapStepWithStepHooks)
                .forEach(stepDefinitions -> testDefinition.getSteps().addAll(stepDefinitions));
        testDefinition.getSteps().addAll(pickleStepFactory.provideAfterTestHooks());
        testDefinition.setFallbackStrategy(fallbackStrategy);
        return testDefinition;
    }

    private String formatLocation(Pickle pickle) {
        PickleLocation pickleLocation = pickle.getLocations().get(0);
        return pickleLocation.getLine() + ":" + pickleLocation.getColumn();
    }

    private List<String> getTagsFromPickle(Pickle pickle) {
        return pickle.getTags().stream()
                .map(PickleTag::getName)
                .collect(Collectors.toList());
    }

    class StandardFallbackStrategy implements TestSuite.FallbackStrategy {

        private int afterStepCounter = 0;
        private List<StepDefinition.StepType> types;
        private List<StepDefinition.StepPhase> phases;

        StandardFallbackStrategy() {
            this.types = Collections.singletonList(StepDefinition.StepType.AFTER);
            this.phases = Arrays.asList(StepDefinition.StepPhase.STEP, StepDefinition.StepPhase.TEST);
        }

        @Override
        public List<StepDefinition.StepType> allowedTypes() {
            return this.types;
        }

        @Override
        public List<StepDefinition.StepPhase> allowedPhases() {
            return this.phases;
        }

        @Override
        public void updateScope(TestSuite.Step executedStep) {
            if (afterStepCounter > 0 && !executedStep.getStepDefinition().getStepType().equals(StepDefinition.StepType.AFTER)) {
                afterStepCounter = -1;
            }
            if (afterStepCounter == 0 && !executedStep.getStepDefinition().getStepType().equals(StepDefinition.StepType.AFTER)) {
                afterStepCounter++;
            }
            if (afterStepCounter > 0 && executedStep.getStepDefinition().getStepType().equals(StepDefinition.StepType.AFTER)) {
                afterStepCounter++;
            }
            if (afterStepCounter < 0) {
                this.phases = Collections.singletonList(StepDefinition.StepPhase.TEST);
            }
        }
    }
}
