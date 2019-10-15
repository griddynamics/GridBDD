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

import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.NamedThreadLocal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is a helper class that able to print short summary after each scenario to info log level
 * By default this bean not configured automatically,
 * use property {@code sprimber.configuration.summary.printer.enable} to enable it
 *
 * @author fparamonov
 */

@Slf4j
@RequiredArgsConstructor
public class TestCaseSummaryPrinter {

    private static final String EMPTY_STRING = "";
    private final StepDefinitionFormatter stepDefinitionFormatter;
    private ThreadLocal<StringBuilder> reportBuilder = new NamedThreadLocal<>("Testcase report builder");
    private ThreadLocal<Integer> depthLevelThreadLocal = new ThreadLocal<>();

    @EventListener
    public void containerNodeStarted(SprimberEventPublisher.ContainerNodeStartedEvent startedEvent) {
        increaseDepthLevel();
        if ("test".equals(startedEvent.getContainerNode().getType())) {
            doWithTestStart(startedEvent.getContainerNode());
        }
    }

    @EventListener
    public void containerNodeFinished(SprimberEventPublisher.ContainerNodeFinishedEvent finishedEvent) {
        decreaseDepthLevel();
        if ("test".equals(finishedEvent.getContainerNode().getType())) {
            doWithTestFinish(finishedEvent.getContainerNode());
        }
    }

    @EventListener
    public void targetNodeCompleted(SprimberEventPublisher.TargetNodeCompletedEvent completedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents() + "\t");
        stringBuilder.append(String.format(" %s", completedEvent.getExecutableNode().getName()));
        stringBuilder.append(String.format(" %s", Arrays.toString(new Collection[]{completedEvent.getExecutableNode().getParameters().values()})));
        stringBuilder.append(String.format(" (%s) ", completedEvent.getExecutableNode().getStatus()));
    }

    @EventListener
    public void targetNodeError(SprimberEventPublisher.TargetNodeErrorEvent errorEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents() + "\t");
        stringBuilder.append(String.format(" %s", errorEvent.getExecutableNode().getName()));
        stringBuilder.append(String.format(" (%s) ", errorEvent.getExecutableNode().getStatus()));
    }

    @EventListener
    public void beforeNodeCompleted(SprimberEventPublisher.BeforeNodeCompletedEvent completedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", completedEvent.getExecutableNode().getName()));
        stringBuilder.append(String.format(" %s", Arrays.toString(new Collection[]{completedEvent.getExecutableNode().getParameters().values()})));
        stringBuilder.append(String.format(" (%s) ", completedEvent.getExecutableNode().getStatus()));
    }

    @EventListener
    public void beforeNodeError(SprimberEventPublisher.BeforeNodeErrorEvent errorEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", errorEvent.getExecutableNode().getName()));
        stringBuilder.append(String.format(" (%s) ", errorEvent.getExecutableNode().getStatus()));
    }

    @EventListener
    public void afterNodeCompleted(SprimberEventPublisher.AfterNodeCompletedEvent completedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", completedEvent.getExecutableNode().getName()));
        stringBuilder.append(String.format(" %s", Arrays.toString(new Collection[]{completedEvent.getExecutableNode().getParameters().values()})));
        stringBuilder.append(String.format(" (%s) ", completedEvent.getExecutableNode().getStatus()));
    }

    @EventListener
    public void afterNodeError(SprimberEventPublisher.AfterNodeErrorEvent errorEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", errorEvent.getExecutableNode().getName()));
        stringBuilder.append(String.format(" (%s) ", errorEvent.getExecutableNode().getStatus()));
    }

    private void doWithTestStart(Node.ContainerNode containerNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n");
        stringBuilder.append(String.format("Test Completed: %s", containerNode.getName()));
        stringBuilder.append(" with status {STATUS}");
        reportBuilder.set(stringBuilder);
    }

    private void doWithTestFinish(Node.ContainerNode containerNode) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n\n");
        String summaryRaw = stringBuilder.toString();
        String summary = summaryRaw.replaceFirst("\\{STATUS}", containerNode.getStatus().name());
        log.info(summary);
        reportBuilder.remove();
    }

    @EventListener
    public void illuminateTestStart(SprimberEventPublisher.TestStartedEvent testStartedEvent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n");
        stringBuilder.append(String.format("Test Completed: %s", testStartedEvent.getTest().getName()));
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
        if (stepFinishedEvent.getStep().isHookStep()) {
            stringBuilder.append(String.format(" %s of scope", stepFinishedEvent.getStep().getStepDefinition().getStepType()));
            stringBuilder.append(String.format(" %s from:", stepFinishedEvent.getStep().getStepDefinition().getStepPhase()));
            stringBuilder.append(String.format(" %s", stepFinishedEvent.getStep().getStepDefinition().getMethod()));
            stringBuilder.append(String.format(" (%s) ", stepFinishedEvent.getStep().getExecutionResult().getStatus()));
        } else {
            stringBuilder.append(String.format("   %s", stepFinishedEvent.getStep().getStepDefinition().getStepType()));
            stringBuilder.append(String.format(" %s", stepDefinitionFormatter.formatStepName(stepFinishedEvent.getStep())));
            stringBuilder.append(String.format(" %s", Arrays.toString(new Collection[]{stepFinishedEvent.getStep().getParameters().values()})));
            stringBuilder.append(String.format(" (%s) ", stepFinishedEvent.getStep().getExecutionResult().getStatus()));
            stringBuilder.append(String.format(" (%s) ", getExceptionMessageIfAny(stepFinishedEvent.getStep().getExecutionResult())));
        }
    }

    private String getIndents() {
        return IntStream.rangeClosed(0, depthLevelThreadLocal.get()).mapToObj(i -> "\t").collect(Collectors.joining());
    }

    private void increaseDepthLevel() {
        int currentLevel = Optional.ofNullable(depthLevelThreadLocal.get()).orElse(1);
        currentLevel++;
        depthLevelThreadLocal.set(currentLevel);
    }

    private void decreaseDepthLevel() {
        int currentLevel = Optional.ofNullable(depthLevelThreadLocal.get()).orElse(1);
        currentLevel--;
        depthLevelThreadLocal.set(currentLevel);
    }

    private String getExceptionMessageIfAny(ExecutionResult executionResult) {
        return executionResult.getOptionalError().map(this::buildExceptionMessage).orElse(EMPTY_STRING);
    }

    private String buildExceptionMessage(Throwable throwable) {
        return Optional.ofNullable(throwable.getMessage()).orElse(throwable.getClass().getName());
    }
}
