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
import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.NamedThreadLocal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * This is a helper class that able to print short summary after each scenario to info log level
 * By default this bean not configured automatically,
 * use property {@code sprimber.configuration.summary.printer.enable} to enable it
 *
 * @author fparamonov
 */

@Slf4j
public class TestCaseSummaryPrinter {

    private static final String EMPTY_STRING = "";
    private ThreadLocal<StringBuilder> reportBuilder = new NamedThreadLocal<>("Testcase report builder");

    @EventListener
    public void illuminateTestStart(SprimberEventPublisher.TestStartedEvent testStartedEvent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n");
        stringBuilder.append(String.format("Test Case Completed: %s", testStartedEvent.getTestDefinition().getName()));
        reportBuilder.set(stringBuilder);
    }

    @EventListener
    public void illuminateTestFinish(SprimberEventPublisher.TestFinishedEvent testFinishedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n\n");
        log.info(stringBuilder.toString());
        reportBuilder.remove();
    }

    @EventListener
    public void illuminateTestStepFinish(SprimberEventPublisher.StepFinishedEvent stepFinishedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        if (stepFinishedEvent.getStepDefinition().getStepType().equals(StepDefinition.StepType.BEFORE) ||
                stepFinishedEvent.getStepDefinition().getStepType().equals(StepDefinition.StepType.AFTER)) {
            stringBuilder.append(String.format(" %s of scope", stepFinishedEvent.getStepDefinition().getStepType()));
            stringBuilder.append(String.format(" %s from:", stepFinishedEvent.getStepDefinition().getStepPhase()));
            stringBuilder.append(String.format(" %s", stepFinishedEvent.getStepDefinition().getMethod()));
            stringBuilder.append(String.format(" (%s) ", stepFinishedEvent.getStepDefinition().getExecutionResult().getStatus()));
        } else {
            stringBuilder.append(String.format("   %s", stepFinishedEvent.getStepDefinition().getStepType()));
            stringBuilder.append(String.format(" %s", stepFinishedEvent.getStepDefinition().getResolvedTextPattern()));
            stringBuilder.append(String.format(" %s", Arrays.toString(new Collection[]{stepFinishedEvent.getStepDefinition().getParameters().values()})));
            stringBuilder.append(String.format(" (%s) ", stepFinishedEvent.getStepDefinition().getExecutionResult().getStatus()));
            stringBuilder.append(String.format(" (%s) ", getExceptionMessageIfAny(stepFinishedEvent.getStepDefinition().getExecutionResult())));
        }
    }

    private String getExceptionMessageIfAny(ExecutionResult executionResult) {
        return executionResult.getOptionalError().map(this::buildExceptionMessage).orElse(EMPTY_STRING);
    }

    private String buildExceptionMessage(Throwable throwable) {
        return Optional.ofNullable(throwable.getMessage()).orElse(throwable.getClass().getName());
    }
}
