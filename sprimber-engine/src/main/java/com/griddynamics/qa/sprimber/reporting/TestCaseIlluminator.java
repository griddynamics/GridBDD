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

import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author fparamonov
 */

@Slf4j
public class TestCaseIlluminator {

    @EventListener
    public void illuminateTestCaseStart(SprimberEventPublisher.TestStartedEvent testStartedEvent) {
        log.debug("Test case started: {}", testStartedEvent.getTest().getName());
    }

    @EventListener
    public void illuminateTestCaseFinish(SprimberEventPublisher.TestFinishedEvent testFinishedEvent) {
        log.debug("Test case finished: '{}' with status {}",
                testFinishedEvent.getTest().getName(), testFinishedEvent.getTest().getExecutionResult().getStatus());
    }

    @EventListener
    public void illuminateTestStepStart(SprimberEventPublisher.StepStartedEvent stepStartedEvent) {
        if (stepStartedEvent.getStep().isHookStep()) {
            log.debug("Test hook of type {} and scope {} started",
                    stepStartedEvent.getStep().getStepDefinition().getStepType(), stepStartedEvent.getStep().getStepDefinition().getStepPhase());
        } else {
            log.debug("Test step started: {}", stepStartedEvent.getStep().getResolvedTextPattern());
        }
    }

    @EventListener
    public void illuminateTestStepFinish(SprimberEventPublisher.StepFinishedEvent stepFinishedEvent) {
        if (stepFinishedEvent.getStep().isHookStep()) {
            log.debug("Test hook of type {} and scope {} finished",
                    stepFinishedEvent.getStep().getStepDefinition().getStepType(), stepFinishedEvent.getStep().getStepDefinition().getStepPhase());
        } else {
            log.debug("Test step finished: {}", stepFinishedEvent.getStep().getResolvedTextPattern());
        }
    }
}
