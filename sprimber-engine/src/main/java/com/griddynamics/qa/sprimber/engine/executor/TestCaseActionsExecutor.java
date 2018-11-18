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

package com.griddynamics.qa.sprimber.engine.executor;

import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.model.TestStep;
import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.scope.annotation.ScenarioComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.griddynamics.qa.sprimber.engine.model.ExecutionResult.Status.FAILED;
import static com.griddynamics.qa.sprimber.engine.model.ExecutionResult.Status.PASSED;

/**
 * This class is actual executor for each action inside of test case such as hooks, steps etc.
 *
 * @author fparamonov
 */

@ScenarioComponent
public class TestCaseActionsExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseActionsExecutor.class);

    private final ErrorMapper errorMapper;
    private final ApplicationContext applicationContext;

    private List<ExecutionResult> currentStageResults = new ArrayList<>();

    public TestCaseActionsExecutor(ErrorMapper errorMapper,
                                   ApplicationContext applicationContext) {
        this.errorMapper = errorMapper;
        this.applicationContext = applicationContext;
    }

    public void cleanStageResults() {
        currentStageResults.clear();
    }

    public List<ExecutionResult> currentFailures() {
        return currentStageResults.stream()
                .filter(executionResult -> !executionResult.getStatus().equals(PASSED))
                .collect(Collectors.toList());
    }

    // TODO: 10/06/2018 to reformat this method to handle exception and pass it to report
    // TODO: 10/06/2018 move hook action to separate class since AOP cannot work correctly with this
    // TODO: 11/15/18 Add exception handling to before/after step hooks
    public ExecutionResult executeStep(TestStep testStep) {
        ExecutionResult stepResult;
        boolean isBeforeStepsPassed = testStep.getBeforeStepActions().stream()
                .map(this::executeHookAction)
                .allMatch(actionExecutionResult -> actionExecutionResult.getStatus().equals(PASSED));
        stepResult = executeMethod(testStep.getStepAction().getMethod(), testStep.getStepArguments());
        boolean isAfterStepsPassed = testStep.getAfterStepActions().stream()
                .map(this::executeHookAction)
                .allMatch(actionExecutionResult -> actionExecutionResult.getStatus().equals(PASSED));
        return isBeforeStepsPassed && isAfterStepsPassed ?
                stepResult.getStatus().equals(PASSED) ? new ExecutionResult(PASSED) : stepResult : new ExecutionResult(FAILED);
    }

    public ExecutionResult executeHookAction(ActionDefinition actionDefinition) {
        return executeMethod(actionDefinition.getMethod(), new Object[0]);
    }

    public ExecutionResult executeMethod(Method method, Object[] args) {
        Object targetObject = applicationContext.getBean(method.getDeclaringClass());
        try {
            ReflectionUtils.invokeMethod(method, targetObject, args);
            ExecutionResult result = new ExecutionResult(PASSED);
            currentStageResults.add(result);
            // TODO: 11/15/18 currently return back the result for compatibility with event listeners
            return result;
        } catch (Throwable throwable) {
            ExecutionResult result = errorMapper.parseThrowable(throwable);
            result.conditionallyPrintStacktrace();
            currentStageResults.add(result);
            LOGGER.trace(result.getErrorMessage());
            LOGGER.error(throwable.getLocalizedMessage());
            return result;
        }
    }
}
