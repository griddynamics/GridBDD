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

package com.griddynamics.qa.sprimber.reporting;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author fparamonov
 */

@Slf4j
@Component
public class TestCaseIlluminator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseIlluminator.class);

    @EventListener
    public void illuminateTestCaseStart(SprimberEventPublisher.TestStartedEvent testStartedEvent) {
        log.debug("Test case started: {}", testStartedEvent.getTestDefinition().getName());
    }

    @EventListener
    public void illuminateTestCaseFinish(SprimberEventPublisher.TestFinishedEvent testFinishedEvent) {
        log.debug("Test case finished: '{}' with status {}",
                testFinishedEvent.getTestDefinition().getName(), testFinishedEvent.getTestDefinition().getExecutionResult().getStatus());
    }

    @EventListener
    public void illuminateTestStepStart(SprimberEventPublisher.StepStartedEvent stepStartedEvent) {
        if (isStepOfTypeHook(stepStartedEvent.getStepDefinition())) {
            log.debug("Test hook of type {} and scope {} started",
                    stepStartedEvent.getStepDefinition().getStepType(), stepStartedEvent.getStepDefinition().getStepPhase());
        } else {
            log.debug("Test step started: {}", stepStartedEvent.getStepDefinition().getResolvedTextPattern());
        }
    }

    @EventListener
    public void illuminateTestStepFinish(SprimberEventPublisher.StepFinishedEvent stepFinishedEvent) {
        if (isStepOfTypeHook(stepFinishedEvent.getStepDefinition())) {
            log.debug("Test hook of type {} and scope {} finished",
                    stepFinishedEvent.getStepDefinition().getStepType(), stepFinishedEvent.getStepDefinition().getStepPhase());
        } else {
            log.debug("Test step finished: {}", stepFinishedEvent.getStepDefinition().getResolvedTextPattern());
        }

    }

    private boolean isStepOfTypeHook(StepDefinition stepDefinition) {
        return stepDefinition.getStepType().equals(StepDefinition.StepType.AFTER) || stepDefinition.getStepType().equals(StepDefinition.StepType.AFTER);
    }
}
