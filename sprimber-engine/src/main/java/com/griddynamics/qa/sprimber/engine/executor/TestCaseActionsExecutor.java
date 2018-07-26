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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * This class is actual executor for each action inside of test case such as hooks, steps etc.
 *
 * @author fparamonov
 */

@Component
public class TestCaseActionsExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseActionsExecutor.class);

    private final ErrorMapper errorMapper;
    private final ApplicationContext applicationContext;

    public TestCaseActionsExecutor(ErrorMapper errorMapper,
                                   ApplicationContext applicationContext) {
        this.errorMapper = errorMapper;
        this.applicationContext = applicationContext;
    }

    // TODO: 10/06/2018 to reformat this method to handle exception and pass it to report
    // TODO: 10/06/2018 move hook action to separate class since AOP cannot work correctly with this
    public ExecutionResult executeStep(TestStep testStep) {
        ExecutionResult stepResult;
        Object targetObject = applicationContext.getBean(testStep.getStepAction().getMethod().getDeclaringClass());
        boolean isBeforeStepsPassed = testStep.getBeforeStepActions().stream()
                .map(this::executeHookAction)
                .allMatch(actionExecutionResult -> actionExecutionResult.equals(ExecutionResult.PASSED));
        try {
            ReflectionUtils.invokeMethod(testStep.getStepAction().getMethod(), targetObject, testStep.getStepArguments());
            stepResult = ExecutionResult.PASSED;
        } catch (Throwable throwable) {
            LOGGER.error(throwable.getLocalizedMessage());
            stepResult = errorMapper.parseThrowable(throwable);
        }
        boolean isAfterStepsPassed = testStep.getAfterStepActions().stream()
                .map(this::executeHookAction)
                .allMatch(actionExecutionResult -> actionExecutionResult.equals(ExecutionResult.PASSED));
        return isBeforeStepsPassed && isAfterStepsPassed ?
                stepResult.equals(ExecutionResult.PASSED) ? ExecutionResult.PASSED : stepResult : ExecutionResult.FAILED;
    }

    public ExecutionResult executeHookAction(ActionDefinition actionDefinition) {
        Object targetObject = applicationContext.getBean(actionDefinition.getMethod().getDeclaringClass());
        try {
            ReflectionUtils.invokeMethod(actionDefinition.getMethod(), targetObject);
            return ExecutionResult.PASSED;
        } catch (Throwable throwable) {
            LOGGER.error(throwable.getLocalizedMessage());
            return errorMapper.parseThrowable(throwable);
        }
    }
}
