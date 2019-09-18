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

package com.griddynamics.qa.sprimber.discovery.support;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuite;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fparamonov
 */
public abstract class AbstractBddStepFactory<SC> extends AbstractStepFactory<SC> {

    public List<TestSuite.Step> provideBeforeTestHooks() {
        return findStepHooksForCurrentStage(StepDefinition.StepType.BEFORE, StepDefinition.StepPhase.TEST)
                .filter(hooksByAvailableTags())
                .map(this::convertHookStepDefinition)
                .collect(Collectors.toList());
    }

    public List<TestSuite.Step> provideAfterTestHooks() {
        return findStepHooksForCurrentStage(StepDefinition.StepType.AFTER, StepDefinition.StepPhase.TEST)
                .filter(hooksByAvailableTags())
                .map(this::convertHookStepDefinition)
                .collect(Collectors.toList());
    }

    public List<TestSuite.Step> wrapStepWithStepHooks(TestSuite.Step step) {
        List<TestSuite.Step> wrappedDefinition =
                findStepHooksForCurrentStage(StepDefinition.StepType.BEFORE, StepDefinition.StepPhase.STEP)
                        .filter(hooksByAvailableTags())
                        .map(this::convertHookStepDefinition)
                        .collect(Collectors.toList());
        wrappedDefinition.add(step);
        List<TestSuite.Step> afterStepHooks =
                findStepHooksForCurrentStage(StepDefinition.StepType.AFTER, StepDefinition.StepPhase.STEP)
                        .filter(hooksByAvailableTags())
                        .map(this::convertHookStepDefinition)
                        .collect(Collectors.toList());
        wrappedDefinition.addAll(afterStepHooks);
        return wrappedDefinition;
    }

    private Stream<StepDefinition> findStepHooksForCurrentStage(StepDefinition.StepType stepType,
                                                                StepDefinition.StepPhase stepPhase) {
        return getStepDefinitions().values().stream()
                .filter(stepDefinition -> stepDefinition.getStepType().equals(stepType))
                .filter(stepDefinition -> stepDefinition.getStepPhase().equals(stepPhase));
    }

    private TestSuite.Step convertHookStepDefinition(StepDefinition stepDefinition) {
        TestSuite.Step step = new TestSuite.Step();
        step.setStepDefinition(stepDefinition);
        step.setName(stepDefinition.getMethod().getName());
        return step;
    }

    protected abstract Predicate<StepDefinition> hooksByAvailableTags();
}
