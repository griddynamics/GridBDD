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

import com.griddynamics.qa.sprimber.common.StepDefinition;
import com.griddynamics.qa.sprimber.common.TestSuite;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class used to instantiate {@link ExecutionContext} with custom pre-initialised runtime objects
 *
 * @author fparamonov
 */

@RequiredArgsConstructor
public class ExecutionContextFactory extends AbstractFactoryBean<ExecutionContext> {

    private final List<TestSuiteDiscovery> testSuiteDiscoveries;
    private final List<StepDefinitionsFactory> stepDefinitionsDiscoveries;

    @Override
    public Class<?> getObjectType() {
        return ExecutionContext.class;
    }

    @Override
    protected ExecutionContext createInstance() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();
        Map<String, StepDefinition> stepDefinitions = exploreStepDefinitions();
        executionContext.getStepDefinitions().putAll(stepDefinitions);
        ((ListableBeanFactory) getBeanFactory()).getBeansOfType(StepFactory.class)
                .values().forEach(stepFactory -> stepFactory.setStepDefinitions(stepDefinitions));
        setupHookOnlyStepFactory(executionContext, stepDefinitions);
        executionContext.getTestSuites().addAll(exploreSuiteDefinitions());
        return executionContext;
    }

    private void setupHookOnlyStepFactory(ExecutionContext executionContext, Map<String, StepDefinition> stepDefinitions) {
        ExecutionContext.HookOnlyStepFactory hookOnlyStepFactory = new ExecutionContext.HookOnlyStepFactory();
        hookOnlyStepFactory.setStepDefinitions(stepDefinitions);
        executionContext.setHookOnlyStepFactory(hookOnlyStepFactory);
    }

    private List<TestSuite> exploreSuiteDefinitions() {
        return testSuiteDiscoveries.stream()
                .map(TestSuiteDiscovery::discover)
                .collect(Collectors.toList());
    }

    private Map<String, StepDefinition> exploreStepDefinitions() {
        return stepDefinitionsDiscoveries.stream()
                .map(StepDefinitionsFactory::getStepDefinitions)
                .collect(HashMap::new, (m, e) -> e.forEach(m::put), Map::putAll);
    }
}
