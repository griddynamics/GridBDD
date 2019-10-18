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
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.model.*;
import io.qameta.allure.util.ResultsUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author fparamonov
 */

@Slf4j
public class AllureSprimber {

    private static final String CUCUMBER_SUITES = "Cucumber Suites";
    private static final int DEFAULT_AWAIT_TERMINATION_SECONDS = 5;
    private static final String ALLURE_WRITER_POOL_PREFIX = "AllureWriter-";
    private static final String TEST_CONTAINER_PREFIX_NAME = "test-container-";
    private static final String EPIC_DEFAULT_NAME = "Cucumber Features";
    private final Map<Node.Status, Status> allureToSprimberStatusMapping;
    private final Map<String, TestParentInfo> testParentsById = new ConcurrentHashMap<>();

    private final AllureLifecycle lifecycle;
    private final ThreadPoolTaskExecutor taskExecutor;

    public AllureSprimber(Map<Node.Status, Status> allureToSprimberStatusMapping,
                          AllureLifecycle lifecycle) {
        this.allureToSprimberStatusMapping = allureToSprimberStatusMapping;
        this.lifecycle = lifecycle;
        this.taskExecutor = initThreadPool();
    }

    private ThreadPoolTaskExecutor initThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(ALLURE_WRITER_POOL_PREFIX);
        taskExecutor.initialize();
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(DEFAULT_AWAIT_TERMINATION_SECONDS);
        return taskExecutor;
    }

    @PreDestroy
    public void destroy() {
        taskExecutor.destroy();
    }

    @EventListener
    public void containerNodeStarted(SprimberEventPublisher.ContainerNodeStartedEvent startedEvent) {
        storeParentInfo(startedEvent.getContainerNode());
        if ("testCase".equals(startedEvent.getContainerNode().getType())) {
            doWithTestCaseStart(startedEvent.getContainerNode());
        }
        if ("test".equals(startedEvent.getContainerNode().getType())) {
            doWithTestStart(startedEvent.getContainerNode());
        }
    }

    @EventListener
    public void containerNodeFinished(SprimberEventPublisher.ContainerNodeFinishedEvent finishedEvent) {
        if ("testCase".equals(finishedEvent.getContainerNode().getType())) {
            doWithTestCaseFinish(finishedEvent.getContainerNode());
        }
        if ("test".equals(finishedEvent.getContainerNode().getType())) {
            doWithTestFinish(finishedEvent.getContainerNode());
        }
    }

    private void storeParentInfo(Node.ContainerNode containerNode) {
        TestParentInfo testParentInfo = new TestParentInfo();
        testParentInfo.setParentName(containerNode.getName());
        testParentInfo.setParentType(containerNode.getType());
        testParentsById.put(containerNode.getRuntimeId(), testParentInfo);
    }

    private void doWithTestCaseStart(Node.ContainerNode containerNode) {
        TestResultContainer testCaseResultContainer = new TestResultContainer();
        testCaseResultContainer.setName(containerNode.getName());
        testCaseResultContainer.setDescription(containerNode.getDescription());
        testCaseResultContainer.setUuid(containerNode.getRuntimeId());

        lifecycle.startTestContainer(testCaseResultContainer);
    }

    private void doWithTestCaseFinish(Node.ContainerNode containerNode) {
        lifecycle.stopTestContainer(containerNode.getRuntimeId());
        lifecycle.writeTestContainer(containerNode.getRuntimeId());
    }

    private void doWithTestStart(Node.ContainerNode containerNode) {
        TestResultContainer testResultContainer = new TestResultContainer();
        testResultContainer.setUuid(TEST_CONTAINER_PREFIX_NAME + containerNode.getRuntimeId());
        TestResult testResult = new TestResult();
        testResult.setName(containerNode.getName());
        testResult.setDescription(containerNode.getDescription());
        testResult.setUuid(containerNode.getRuntimeId());
        testResult.setHistoryId(containerNode.getHistoryId());
        testResult.setFullName(String.valueOf(containerNode.getAttributes().getOrDefault("testLocation", "")));

        lifecycle.startTestContainer(testResultContainer);
        lifecycle.scheduleTestCase(testResultContainer.getUuid(), testResult);
        lifecycle.startTestCase(containerNode.getRuntimeId());

        List<Label> labels = processTestLabels(containerNode);
        List<Link> links = processTestLinks(containerNode);
        testResult.setLabels(labels);
        testResult.setLinks(links);
    }

    private void doWithTestFinish(Node.ContainerNode containerNode) {
        Node.Status status = containerNode.getStatus();
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(null);

        lifecycle.updateTestCase(containerNode.getRuntimeId(), scenarioResult -> {
            scenarioResult.setStatus(allureToSprimberStatusMapping.get(status));
            statusDetails.ifPresent(scenarioResult::setStatusDetails);
        });
        lifecycle.stopTestCase(containerNode.getRuntimeId());
        lifecycle.stopTestContainer(TEST_CONTAINER_PREFIX_NAME + containerNode.getRuntimeId());

        taskExecutor.execute(() -> {
            lifecycle.writeTestCase(containerNode.getRuntimeId());
            lifecycle.writeTestContainer(TEST_CONTAINER_PREFIX_NAME + containerNode.getRuntimeId());
        });
    }

    // TODO: 2019-10-17 update this method to show the links
    private List<Link> processTestLinks(Node.ContainerNode containerNode) {
        return Collections.emptyList();
    }

    private List<Label> processTestLabels(Node.ContainerNode containerNode) {
        List<Label> labels = new ArrayList<>();
        Label epicLabel = ResultsUtils.createEpicLabel(containerNode.getMeta().getSingleValueOrDefault("epic", EPIC_DEFAULT_NAME));
        Label featureLabel = ResultsUtils.createFeatureLabel(
                containerNode.getMeta().getSingleValueOrDefault("feature", testParentsById.get(containerNode.getParentId()).getParentName()));
        Label storyLabel = ResultsUtils.createStoryLabel(containerNode.getMeta().getSingleValueOrDefault("story", containerNode.getName()));

        Label packageLabel = ResultsUtils.createPackageLabel(
                containerNode.getMeta().getSingleValueOrDefault("package", testParentsById.get(containerNode.getParentId()).getParentName()));

        Label parentSuiteLabel = ResultsUtils.createParentSuiteLabel(containerNode.getMeta().getSingleValueOrDefault("parentSuite", CUCUMBER_SUITES));
        Label suiteLabel = ResultsUtils.createSuiteLabel(
                containerNode.getMeta().getSingleValueOrDefault("suite", testParentsById.get(containerNode.getParentId()).getParentName()));
        Label subSuiteLabel = ResultsUtils.createSubSuiteLabel(containerNode.getMeta().getSingleValueOrDefault("subSuite", containerNode.getName()));

        Label severityLabel = ResultsUtils.createSeverityLabel(containerNode.getMeta().getSingleValueOrDefault("severity", SeverityLevel.NORMAL.value()));

        labels.add(epicLabel);
        labels.add(featureLabel);
        labels.add(storyLabel);
        labels.add(packageLabel);
        labels.add(parentSuiteLabel);
        labels.add(suiteLabel);
        labels.add(subSuiteLabel);
        labels.add(severityLabel);
        return labels;
    }

//    @EventListener
//    public void codeStyleStepStarted(SprimberEventPublisher.UtilityStepStartedEvent utilityStepStartedEvent) {
//        StepResult stepResult = new StepResult();
//        stepResult.setName(utilityStepStartedEvent.getStep().getName());
//        lifecycle.startStep(utilityStepStartedEvent.getStep().getRuntimeId(), stepResult);
//    }
//
//    // TODO: 2019-10-17 handle the statuses transition
//    @EventListener
//    public void codeStyleStepFinished(SprimberEventPublisher.UtilityStepFinishedEvent utilityStepFinishedEvent) {
//        ExecutionResult result = utilityStepFinishedEvent.getStep().getExecutionResult();
//        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(result.getOptionalError().orElse(null));
//        lifecycle.updateStep(step -> {
//            step.setStatus(allureToSprimberStatusMapping.get(result.getStatus()));
//            statusDetails.ifPresent(step::setStatusDetails);
//        });
//        lifecycle.stopStep();
//    }

    @EventListener
    public void targetNodeStarted(SprimberEventPublisher.TargetNodeStartedEvent startedEvent) {
        startStep(startedEvent.getExecutableNode());
    }

    @EventListener
    public void targetNodeCompleted(SprimberEventPublisher.TargetNodeCompletedEvent completedEvent) {
        completeStep(completedEvent.getExecutableNode());
    }

    @EventListener
    public void targetNodeError(SprimberEventPublisher.TargetNodeErrorEvent errorEvent) {
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(errorEvent.getExecutableNode().getThrowable());
        lifecycle.updateStep(errorEvent.getExecutableNode().getRuntimeId(),
                stepResult -> {
                    stepResult.setStatus(allureToSprimberStatusMapping.get(errorEvent.getExecutableNode().getStatus()));
                    statusDetails.ifPresent(stepResult::setStatusDetails);
                });
        lifecycle.updateTestCase(testResult -> statusDetails.ifPresent(testResult::setStatusDetails));
        lifecycle.stopStep(errorEvent.getExecutableNode().getRuntimeId());
    }

    @EventListener
    public void beforeNodeStarted(SprimberEventPublisher.BeforeNodeStartedEvent startedEvent) {
        if ("test".equals(testParentsById.get(startedEvent.getExecutableNode().getParentId()).getParentType())) {
            FixtureResult fixtureResult = new FixtureResult();
            fixtureResult.setName(startedEvent.getExecutableNode().getName());
            fixtureResult.setParameters(convertStepParameters(startedEvent.getExecutableNode().getParameters()));
            lifecycle.startPrepareFixture(TEST_CONTAINER_PREFIX_NAME + startedEvent.getExecutableNode().getParentId(),
                    startedEvent.getExecutableNode().getRuntimeId(), fixtureResult);
        } else {
            startStep(startedEvent.getExecutableNode());
        }
    }

    @EventListener
    public void beforeNodeCompleted(SprimberEventPublisher.BeforeNodeCompletedEvent completedEvent) {
        if ("test".equals(testParentsById.get(completedEvent.getExecutableNode().getParentId()).getParentType())) {
            lifecycle.updateFixture(completedEvent.getExecutableNode().getRuntimeId(),
                    fixtureResult -> fixtureResult.setStatus(allureToSprimberStatusMapping.get(completedEvent.getExecutableNode().getStatus())));
            lifecycle.stopFixture(completedEvent.getExecutableNode().getRuntimeId());
            lifecycle.setCurrentTestCase(completedEvent.getExecutableNode().getParentId());
        } else {
            completeStep(completedEvent.getExecutableNode());
        }
    }

    @EventListener
    public void beforeNodeError(SprimberEventPublisher.BeforeNodeErrorEvent errorEvent) {
        stopStepWithError(errorEvent.getExecutableNode());
    }

    @EventListener
    public void afterNodeStarted(SprimberEventPublisher.AfterNodeStartedEvent startedEvent) {
        if ("test".equals(testParentsById.get(startedEvent.getExecutableNode().getParentId()).getParentType())) {
            FixtureResult fixtureResult = new FixtureResult();
            fixtureResult.setName(startedEvent.getExecutableNode().getName());
            fixtureResult.setParameters(convertStepParameters(startedEvent.getExecutableNode().getParameters()));
            lifecycle.startTearDownFixture(TEST_CONTAINER_PREFIX_NAME + startedEvent.getExecutableNode().getParentId(),
                    startedEvent.getExecutableNode().getRuntimeId(), fixtureResult);
        } else {
            startStep(startedEvent.getExecutableNode());
        }
    }

    @EventListener
    public void afterNodeCompleted(SprimberEventPublisher.AfterNodeCompletedEvent completedEvent) {
        if ("test".equals(testParentsById.get(completedEvent.getExecutableNode().getParentId()).getParentType())) {
            lifecycle.updateFixture(completedEvent.getExecutableNode().getRuntimeId(),
                    fixtureResult -> fixtureResult.setStatus(allureToSprimberStatusMapping.get(completedEvent.getExecutableNode().getStatus())));
            lifecycle.stopFixture(completedEvent.getExecutableNode().getRuntimeId());
        } else {
            completeStep(completedEvent.getExecutableNode());
        }
    }

    @EventListener
    public void afterNodeError(SprimberEventPublisher.AfterNodeErrorEvent errorEvent) {
        stopStepWithError(errorEvent.getExecutableNode());
    }

    private void startStep(Node.ExecutableNode executableNode) {
        StepResult stepResult = new StepResult();
        stepResult.setName(executableNode.getName());
        stepResult.setParameters(convertStepParameters(executableNode.getParameters()));
        lifecycle.startStep(executableNode.getRuntimeId(), stepResult);
        StringBuilder dataTableCsv = new StringBuilder();
        executableNode.getAttributes().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("stepData"))
                .forEach(entry -> dataTableCsv.append(entry.getKey()).append("\t").append(entry.getValue()).append("\n"));

        lifecycle.addAttachment("Step Attributes", "text/tab-separated-values", "csv", dataTableCsv.toString().getBytes());
        attachStepDataParameterIfPresent(String.valueOf(executableNode.getAttributes().getOrDefault("stepData", "")));
    }

    private void completeStep(Node.ExecutableNode executableNode) {
        lifecycle.updateStep(executableNode.getRuntimeId(),
                stepResult -> stepResult.setStatus(allureToSprimberStatusMapping.get(executableNode.getStatus()))
        );
        lifecycle.stopStep(executableNode.getRuntimeId());
    }

    private void stopStepWithError(Node.ExecutableNode executableNode) {
        if ("test".equals(testParentsById.get(executableNode.getParentId()).getParentType())) {
            Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(executableNode.getThrowable());
            lifecycle.updateFixture(executableNode.getRuntimeId(), fixtureResult -> {
                fixtureResult.setStatus(allureToSprimberStatusMapping.get(executableNode.getStatus()));
                statusDetails.ifPresent(fixtureResult::setStatusDetails);
            });
            lifecycle.stopFixture(executableNode.getRuntimeId());
            lifecycle.setCurrentTestCase(executableNode.getParentId());
        } else {
            Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(executableNode.getThrowable());
            lifecycle.updateStep(executableNode.getRuntimeId(),
                    stepResult -> {
                        stepResult.setStatus(allureToSprimberStatusMapping.get(executableNode.getStatus()));
                        statusDetails.ifPresent(stepResult::setStatusDetails);
                    });
            lifecycle.stopStep(executableNode.getRuntimeId());
        }
    }

    private List<Parameter> convertStepParameters(Map<String, Object> parameters) {
        return parameters.entrySet().stream()
                .map(this::getParameter)
                .collect(Collectors.toList());
    }

    private Parameter getParameter(Map.Entry<String, Object> entry) {
        Parameter parameter = new Parameter();
        parameter.setName(entry.getKey());
        parameter.setValue(String.valueOf(entry.getValue()));
        return parameter;
    }

    private void attachStepDataParameterIfPresent(String stepData) {
        if (isNotBlank(stepData)) {
            final String attachmentSource = lifecycle
                    .prepareAttachment("Data table", "text/tab-separated-values", "csv");
            lifecycle.writeAttachment(attachmentSource,
                    new ByteArrayInputStream(stepData.getBytes(Charset.forName("UTF-8"))));
        }
    }

    @Data
    private class TestParentInfo {
        private String parentName;
        private String parentType;
    }
}
