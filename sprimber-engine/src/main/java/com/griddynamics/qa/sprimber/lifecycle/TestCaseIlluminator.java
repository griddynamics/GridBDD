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

package com.griddynamics.qa.sprimber.lifecycle;

import com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase.TestCaseFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase.TestCaseStartedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testhook.TestHookFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testhook.TestHookStartedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.teststep.TestStepFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.teststep.TestStepStartedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testsuite.TestSuiteFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testsuite.TestSuiteStartedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.loader.DefinitionLoadingFinishEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.loader.DefinitionLoadingStartEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.processor.ResourceProcessingFinishEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.processor.ResourceProcessingStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author fparamonov
 */
@Component
public class TestCaseIlluminator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseIlluminator.class);

    @EventListener
    public void illuminateDefinitionLoadingStart(DefinitionLoadingStartEvent startEvent) {
        LOGGER.debug("Definition loading starts from {}", startEvent.getSource().getClass());
    }

    @EventListener
    public void illuminateDefinitionLoadingFinish(DefinitionLoadingFinishEvent finishEvent) {
        LOGGER.debug("Definition loading finished from {}", finishEvent.getSource().getClass());
    }

    @EventListener
    public void illuminateDefinitionLoadingStart(ResourceProcessingStartEvent startEvent) {
        LOGGER.debug("Resource processing starts from {}", startEvent.getSource().getClass());
    }

    @EventListener
    public void illuminateDefinitionLoadingFinish(ResourceProcessingFinishEvent finishEvent) {
        LOGGER.debug("Resource processing finished from {}", finishEvent.getSource().getClass());
    }

    @EventListener
    public void illuminateTestSuiteStart(TestSuiteStartedEvent startEvent) {
        LOGGER.debug("Test suite started: {}", startEvent.getTestSuite().getRuntimeId());
    }

    @EventListener
    public void illuminateTestSuiteFinish(TestSuiteFinishedEvent finishEvent) {
        LOGGER.debug("Test suite finished: '{}' with status {}",
                finishEvent.getTestSuite().getRuntimeId(), finishEvent.getExecutionResult().getStatus());
    }

    @EventListener
    public void illuminateTestCaseStart(TestCaseStartedEvent startEvent) {
        LOGGER.debug("Test case started: {}", startEvent.getTestCase().getName());
    }

    @EventListener
    public void illuminateTestCaseFinish(TestCaseFinishedEvent finishEvent) {
        LOGGER.debug("Test case finished: '{}' with status {}",
                finishEvent.getTestCase().getName(), finishEvent.getExecutionResult().getStatus());
    }

    @EventListener
    public void illuminateTestStepStart(TestStepStartedEvent startEvent) {
        LOGGER.debug("Test step started: {}", startEvent.getTestStep().getActualText());
    }

    @EventListener
    public void illuminateTestStepFinish(TestStepFinishedEvent finishEvent) {
        LOGGER.debug("Test step finished: {}", finishEvent.getTestStep().getActualText());
    }

    @EventListener
    public void illuminateTestHookStart(TestHookStartedEvent startEvent) {
        LOGGER.debug("Test hook of type {} and scope {} started",
                startEvent.getHookDefinition().getActionType(), startEvent.getHookDefinition().getActionScope());
    }

    @EventListener
    public void illuminateTestHookFinish(TestHookFinishedEvent finishEvent) {
        LOGGER.debug("Test hook of type {} and scope {} finished",
                finishEvent.getHookDefinition().getActionType(), finishEvent.getHookDefinition().getActionScope());
    }
}
