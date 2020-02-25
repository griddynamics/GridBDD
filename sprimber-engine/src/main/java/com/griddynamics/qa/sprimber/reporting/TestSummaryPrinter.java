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
import com.griddynamics.qa.sprimber.engine.TreeExecutorContext;
import com.griddynamics.qa.sprimber.runtime.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
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
public abstract class TestSummaryPrinter {

    private static final String STRING_BUILDER_NAME = "stringBuilder";
    private static final String LEVEL_COUNTER_NAME = "levelCounter";
    private final ExecutionContext executionContext;

    private final TreeExecutorContext executorContext;
    private final AtomicInteger executedCount = new AtomicInteger(0);
    private final AtomicInteger exceptionsCount = new AtomicInteger(0);

    /**
     * Should return true in case when the node relates to the test phase like step, test hook, etc
     *
     * @param node
     * @return
     */
    abstract boolean isNodeLevelAboveTest(Node node);

    abstract String getTestStageName();

    void addSuccessNodeRow(Node node) {
        if (isNodeLevelAboveTest(node)) return;
        StringBuilder stringBuilder = (StringBuilder) executorContext.getObjectForStage(node, getTestStageName(), STRING_BUILDER_NAME);
        stringBuilder.append("\n")
                .append(getIndents(node))
                .append(String.format(" %s", node.getName()))
                .append(String.format(" (%s) ", node.getCurrentState()));
        printMethodParameters(node.getMethodParameters(), stringBuilder);
    }

    void addErrorNodeRow(Node node) {
        if (isNodeLevelAboveTest(node)) return;
        exceptionsCount.incrementAndGet();
        StringBuilder stringBuilder =  (StringBuilder) executorContext.getObjectForStage(node, getTestStageName(), STRING_BUILDER_NAME);
        stringBuilder.append("\n")
                .append(getIndents(node))
                .append(String.format(" %s", node.getName()))
                .append(String.format(" (%s) \n", node.getCurrentState()));
        node.getThrowable().ifPresent(throwable -> stringBuilder.append(String.format("Description: %s ", throwable.getLocalizedMessage())));
    }

    void doWithTestStart(Node containerNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n")
                .append(String.format("Completed: '%s'", containerNode.getName()))
                .append(" with status {STATUS}");
        executorContext.addObjectForStage(containerNode, getTestStageName(), STRING_BUILDER_NAME, stringBuilder);
        executorContext.addObjectForStage(containerNode, getTestStageName(), LEVEL_COUNTER_NAME, new AtomicInteger(0));
    }

    void doWithTestFinish(Node containerNode) {
        executedCount.incrementAndGet();
        StringBuilder stringBuilder = (StringBuilder) executorContext.getObjectForStage(containerNode, getTestStageName(), STRING_BUILDER_NAME);
        stringBuilder.append("\n")
                .append(String.format("Executed '%d' out of '%d'", executedCount.get(), executionContext.getStatistic().preparedCountByStage(getTestStageName())))
                .append(String.format("\nCurrent pass rate %.2f%%", getPassedRate()))
                .append("\n\n");
        String summaryRaw = stringBuilder.toString();
        String summary = summaryRaw.replaceFirst("\\{STATUS}", String.valueOf(containerNode.getCurrentState()));
        log.info(summary);
    }

    void increaseDepthLevel(Node currentNode) {
        if (isNodeLevelAboveTest(currentNode)) return;
        AtomicInteger currentLevel = (AtomicInteger) executorContext.getObjectForStage(currentNode, getTestStageName(), LEVEL_COUNTER_NAME);
        currentLevel.incrementAndGet();
    }

    void decreaseDepthLevel(Node currentNode) {
        if (isNodeLevelAboveTest(currentNode)) return;
        AtomicInteger currentLevel = (AtomicInteger) executorContext.getObjectForStage(currentNode, getTestStageName(), LEVEL_COUNTER_NAME);
        currentLevel.decrementAndGet();
    }

    private String getIndents(Node currentNode) {
        AtomicInteger currentLevel = (AtomicInteger) executorContext.getObjectForStage(currentNode, getTestStageName(), LEVEL_COUNTER_NAME);
        return IntStream.rangeClosed(0, currentLevel.get()).mapToObj(i -> "\t").collect(Collectors.joining());
    }

    private void printMethodParameters(Map<String, Object> parameters, StringBuilder stringBuilder) {
        parameters.forEach((k, v) -> stringBuilder
                .append("\nValue:\n")
                .append(v));
    }

    private double getPassedRate() {
        return (1.0 - (double) exceptionsCount.get() / (double) executionContext.getStatistic().preparedCountByStage("test")) * 100;
    }
}
