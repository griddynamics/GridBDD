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

import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.runtime.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.NamedThreadLocal;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

    private final ExecutionContext executionContext;

    private final ThreadLocal<StringBuilder> reportBuilder = ThreadLocal.withInitial(StringBuilder::new);
    private final ThreadLocal<Integer> depthLevelThreadLocal = ThreadLocal.withInitial(() -> 0);
    private final AtomicInteger executedCount = new AtomicInteger(0);
    private final AtomicInteger exceptionsCount = new AtomicInteger(0);

    @EventListener
    public void containerNodeStarted(SprimberEventPublisher.ContainerNodeStartedEvent startedEvent) {
        increaseDepthLevel();
        if ("test".equals(startedEvent.getNode().getRole())) {
            doWithTestStart(startedEvent.getNode());
        }
    }

    @EventListener
    public void containerNodeFinished(SprimberEventPublisher.ContainerNodeFinishedEvent finishedEvent) {
        decreaseDepthLevel();
        if ("test".equals(finishedEvent.getNode().getRole())) {
            doWithTestFinish(finishedEvent.getNode());
        }
    }

    @EventListener
    public void targetNodeCompleted(SprimberEventPublisher.TargetNodeCompletedEvent completedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n")
                .append(getIndents())
                .append("\t")
                .append(String.format(" %s", completedEvent.getNode().getName()))
                .append(String.format(" (%s) ", completedEvent.getNode().getCurrentState()));
        printMethodParameters(completedEvent.getNode().getMethodParameters());
    }

    @EventListener
    public void targetNodeError(SprimberEventPublisher.TargetNodeErrorEvent errorEvent) {
        exceptionsCount.incrementAndGet();
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents() + "\t");
        stringBuilder.append(String.format(" %s", errorEvent.getNode().getName()));
        stringBuilder.append(String.format(" (%s) \n", errorEvent.getNode().getCurrentState()));
        errorEvent.getNode().getThrowable().ifPresent(throwable -> stringBuilder.append(String.format("Description: %s ", throwable.getLocalizedMessage())));
    }

    @EventListener
    public void beforeNodeCompleted(SprimberEventPublisher.BeforeNodeCompletedEvent completedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", completedEvent.getNode().getName()));
        stringBuilder.append(String.format(" (%s) ", completedEvent.getNode().getCurrentState()));
        printMethodParameters(completedEvent.getNode().getMethodParameters());
    }

    @EventListener
    public void beforeNodeError(SprimberEventPublisher.BeforeNodeErrorEvent errorEvent) {
        exceptionsCount.incrementAndGet();
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", errorEvent.getNode().getName()));
        stringBuilder.append(String.format(" (%s) ", errorEvent.getNode().getCurrentState()));
    }

    @EventListener
    public void afterNodeCompleted(SprimberEventPublisher.AfterNodeCompletedEvent completedEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", completedEvent.getNode().getName()));
        stringBuilder.append(String.format(" (%s) ", completedEvent.getNode().getCurrentState()));
        printMethodParameters(completedEvent.getNode().getMethodParameters());
    }

    @EventListener
    public void afterNodeError(SprimberEventPublisher.AfterNodeErrorEvent errorEvent) {
        exceptionsCount.incrementAndGet();
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(getIndents());
        stringBuilder.append(String.format(" %s", errorEvent.getNode().getName()));
        stringBuilder.append(String.format(" (%s) ", errorEvent.getNode().getCurrentState()));
    }

    private void printMethodParameters(Map<String, Object> parameters) {
        parameters.forEach((k,v) -> reportBuilder.get()
                .append("\nValue:\n")
                .append(v));
    }

    private void doWithTestStart(Node containerNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n");
        stringBuilder.append(String.format("Test # Completed: '%s'", containerNode.getName()));
        stringBuilder.append(" with status {STATUS}");
        reportBuilder.set(stringBuilder);
    }

    private void doWithTestFinish(Node containerNode) {
        executedCount.incrementAndGet();
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n")
                .append(String.format("Executed '%d' out of '%d'", executedCount.get(), executionContext.getStatistic().preparedCountByStage("test")))
                .append(String.format("\nCurrent pass rate %.2f%%", getPassedRate()))
                .append("\n\n");
        String summaryRaw = stringBuilder.toString();
        String summary = summaryRaw.replaceFirst("\\{STATUS}", String.valueOf(containerNode.getCurrentState()));
        log.info(summary);
        reportBuilder.remove();
    }

    private double getPassedRate() {
        return (1.0 - (double) exceptionsCount.get() / (double) executionContext.getStatistic().preparedCountByStage("test")) * 100;
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

    private String buildExceptionMessage(Throwable throwable) {
        return Optional.ofNullable(throwable.getMessage()).orElse(throwable.getClass().getName());
    }
}
