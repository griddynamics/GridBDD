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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

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
@RequiredArgsConstructor
public class AllureSprimber {

    private static final String CUCUMBER_SUITES = "Cucumber Suites";
    private static final String TEST_CONTAINER_PREFIX_NAME = "test-";
    private static final String EPIC_DEFAULT_NAME = "Cucumber Features";
    private static final String TEST_CASE_CONTAINER_PREFIX_NAME = "test-case-";
    private final Map<String, TestParentInfo> testParentsById = new ConcurrentHashMap<>();
    private final AllureLifecycle lifecycle;
    private final Set<String> nonPrintableExceptions;

    @EventListener
    public void stageStarted(SprimberEventPublisher.ContainerNodeStartedEvent startedEvent) {
        if (startedEvent.getNode().isEmptyHolder()) return;
        storeParentInfo(startedEvent.getNode());
        if ("testCase".equals(startedEvent.getNode().getRole())) {
            doWithTestCaseStart(startedEvent.getNode());
        }
        if ("test".equals(startedEvent.getNode().getRole())) {
            doWithTestStart(startedEvent.getNode());
        }
        if ("stepContainer".equals(startedEvent.getNode().getRole())) {
            log.info(startedEvent.getNode().getRuntimeId().toString());
            StepResult stepResult = new StepResult();
            lifecycle.startStep(startedEvent.getNode().getParentId().toString(),
                    startedEvent.getNode().getRuntimeId().toString(), stepResult);
        }
    }

    @EventListener
    public void stageFinished(SprimberEventPublisher.ContainerNodeFinishedEvent finishedEvent) {
        if (finishedEvent.getNode().isEmptyHolder()) return;
        if ("testCase".equals(finishedEvent.getNode().getRole())) {
            doWithTestCaseFinish(finishedEvent.getNode());
        }
        if ("test".equals(finishedEvent.getNode().getRole())) {
            doWithTestFinish(finishedEvent.getNode());
        }
        if ("stepContainer".equals(finishedEvent.getNode().getRole())) {
            log.info(finishedEvent.getNode().getRuntimeId().toString() + ":" + finishedEvent.getNode().getCurrentState());

            lifecycle.updateStep(finishedEvent.getNode().getRuntimeId().toString(),
                    stepResult -> {
                        if (finishedEvent.getNode().isCompletedSuccessfully()) {
                            stepResult.setStatus(Status.PASSED);
                        }
                        if (finishedEvent.getNode().isCompletedWithSkip()) {
                            stepResult.setStatus(Status.SKIPPED);
                        }
                        if (finishedEvent.getNode().isCompletedExceptionally()) {
                            stepResult.setStatus(finishedEvent.getNode().getThrowable().map(this::mapThrowable).orElse(Status.BROKEN));
                            Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(finishedEvent.getNode().getThrowable().get());
                            statusDetails.ifPresent(stepResult::setStatusDetails);
                        }
                    }
            );
            lifecycle.stopStep(finishedEvent.getNode().getRuntimeId().toString());
        }
    }

    private void storeParentInfo(Node node) {
        TestParentInfo testParentInfo = new TestParentInfo();
        testParentInfo.setParentName(node.getName());
        testParentInfo.setParentType(node.getRole());
        testParentsById.put(node.getRuntimeId().toString(), testParentInfo);
    }

    private void doWithTestCaseStart(Node node) {
        TestResultContainer testCaseResultContainer = new TestResultContainer();
        testCaseResultContainer.setName(node.getName());
        testCaseResultContainer.setDescription(node.getDescription());
        testCaseResultContainer.setUuid(TEST_CASE_CONTAINER_PREFIX_NAME + node.getRuntimeId().toString());

        lifecycle.startTestContainer(testCaseResultContainer);
    }

    private void doWithTestCaseFinish(Node node) {
        lifecycle.stopTestContainer(TEST_CASE_CONTAINER_PREFIX_NAME + node.getRuntimeId().toString());
        lifecycle.writeTestContainer(TEST_CASE_CONTAINER_PREFIX_NAME + node.getRuntimeId().toString());
    }

    private void doWithTestStart(Node node) {
        TestResultContainer testResultContainer = new TestResultContainer();
        testResultContainer.setUuid(TEST_CONTAINER_PREFIX_NAME + node.getRuntimeId());
        testResultContainer.setDescription(node.getDescription());
        testResultContainer.setName(node.getName());

        TestResult testResult = new TestResult();
        testResult.setName(node.getName());
        testResult.setDescription(node.getDescription());
        testResult.setUuid(node.getRuntimeId().toString());
        testResult.setHistoryId(node.getHistoryId());
        testResult.setFullName(String.valueOf(node.getAttribute("testLocation").orElse("")));

        List<Label> labels = processTestLabels(node);
        List<Link> links = processTestLinks(node);
        testResult.setLabels(labels);
        testResult.setLinks(links);

        lifecycle.startTestContainer(TEST_CASE_CONTAINER_PREFIX_NAME + node.getParentId().toString(), testResultContainer);
        lifecycle.scheduleTestCase(testResultContainer.getUuid(), testResult);
        lifecycle.startTestCase(node.getRuntimeId().toString());
    }

    private void doWithTestFinish(Node node) {
        lifecycle.updateTestCase(node.getRuntimeId().toString(), scenarioResult -> {
            if (node.isCompletedSuccessfully()) {
                scenarioResult.setStatus(Status.PASSED);
            }
            if (node.isCompletedExceptionally()) {
                scenarioResult.setStatus(node.getThrowable().map(this::mapThrowable).orElse(Status.BROKEN));
                Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(null);
                statusDetails.ifPresent(scenarioResult::setStatusDetails);
            }
        });
        lifecycle.stopTestCase(node.getRuntimeId().toString());
        lifecycle.stopTestContainer(TEST_CONTAINER_PREFIX_NAME + node.getRuntimeId());

        lifecycle.writeTestCase(node.getRuntimeId().toString());
        lifecycle.writeTestContainer(TEST_CONTAINER_PREFIX_NAME + node.getRuntimeId());
    }

    // TODO: 2019-10-17 update this method to show the links
    private List<Link> processTestLinks(Node node) {
        return Collections.emptyList();
    }

    private List<Label> processTestLabels(Node node) {
        return node.getAttribute("meta").map(o -> {
            Node.Meta meta = (Node.Meta) o;
            List<Label> labels = new ArrayList<>();
            Label epicLabel = ResultsUtils.createEpicLabel(meta.getSingleValueOrDefault("epic", EPIC_DEFAULT_NAME));
            Label featureLabel = ResultsUtils.createFeatureLabel(
                    meta.getSingleValueOrDefault("feature", testParentsById.get(node.getParentId().toString()).getParentName()));
            Label storyLabel = ResultsUtils.createStoryLabel(meta.getSingleValueOrDefault("story", node.getName()));

            Label packageLabel = ResultsUtils.createPackageLabel(
                    meta.getSingleValueOrDefault("package", testParentsById.get(node.getParentId().toString()).getParentName()));

            Label parentSuiteLabel = ResultsUtils.createParentSuiteLabel(meta.getSingleValueOrDefault("parentSuite", CUCUMBER_SUITES));
            Label suiteLabel = ResultsUtils.createSuiteLabel(
                    meta.getSingleValueOrDefault("suite", testParentsById.get(node.getParentId().toString()).getParentName()));
            Label subSuiteLabel = ResultsUtils.createSubSuiteLabel(meta.getSingleValueOrDefault("subSuite", node.getName()));

            Label severityLabel = ResultsUtils.createSeverityLabel(meta.getSingleValueOrDefault("severity", SeverityLevel.NORMAL.value()));

            Label threadLabel = ResultsUtils.createThreadLabel();
            Label hostLabel = ResultsUtils.createHostLabel();

            labels.add(epicLabel);
            labels.add(featureLabel);
            labels.add(storyLabel);
            labels.add(packageLabel);
            labels.add(parentSuiteLabel);
            labels.add(suiteLabel);
            labels.add(subSuiteLabel);
            labels.add(severityLabel);
            labels.add(threadLabel);
            labels.add(hostLabel);
            return labels;
        }).orElse(Collections.emptyList());
    }

    @EventListener
    public void targetNodeStarted(SprimberEventPublisher.TargetNodeStartedEvent startedEvent) {
        startStep(startedEvent.getNode());
        lifecycle.updateStep(startedEvent.getNode().getParentId().toString(),
                stepResult -> stepResult.setName(startedEvent.getNode().getName()));
    }

    @EventListener
    public void targetNodeCompleted(SprimberEventPublisher.TargetNodeCompletedEvent completedEvent) {
        completeStep(completedEvent.getNode());
    }

    @EventListener
    public void targetNodeError(SprimberEventPublisher.TargetNodeErrorEvent errorEvent) {
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(errorEvent.getNode().getThrowable().get());
        lifecycle.updateStep(errorEvent.getNode().getRuntimeId().toString(),
                stepResult -> {
                    stepResult.setStatus(errorEvent.getNode().getThrowable().map(this::mapThrowable).orElse(Status.BROKEN));
                    errorEvent.getNode().getThrowable().ifPresent(this::attachExceptionMessage);
                    statusDetails.ifPresent(stepResult::setStatusDetails);
                });
        lifecycle.updateTestCase(testResult -> statusDetails.ifPresent(testResult::setStatusDetails));
        lifecycle.stopStep(errorEvent.getNode().getRuntimeId().toString());
    }

    @EventListener
    public void beforeNodeStarted(SprimberEventPublisher.BeforeNodeStartedEvent startedEvent) {
        if ("test".equals(testParentsById.get(startedEvent.getNode().getParentId().toString()).getParentType())) {
            FixtureResult fixtureResult = new FixtureResult();
            fixtureResult.setName(startedEvent.getNode().getName());
            fixtureResult.setParameters(convertStepParameters(startedEvent.getNode().getMethodParameters()));
            lifecycle.startPrepareFixture(TEST_CONTAINER_PREFIX_NAME + startedEvent.getNode().getParentId(),
                    startedEvent.getNode().getRuntimeId().toString(), fixtureResult);
        } else {
            startStep(startedEvent.getNode());
        }
    }

    @EventListener
    public void beforeNodeCompleted(SprimberEventPublisher.BeforeNodeCompletedEvent completedEvent) {
        if ("test".equals(testParentsById.get(completedEvent.getNode().getParentId().toString()).getParentType())) {
            updateAndStopNonExceptionFixture(completedEvent.getNode());
            lifecycle.setCurrentTestCase(completedEvent.getNode().getParentId().toString());
        } else {
            completeStep(completedEvent.getNode());
        }
    }

    @EventListener
    public void beforeNodeError(SprimberEventPublisher.BeforeNodeErrorEvent errorEvent) {
        stopStepWithError(errorEvent.getNode());
    }

    @EventListener
    public void afterNodeStarted(SprimberEventPublisher.AfterNodeStartedEvent startedEvent) {
        if ("test".equals(testParentsById.get(startedEvent.getNode().getParentId().toString()).getParentType())) {
            FixtureResult fixtureResult = new FixtureResult();
            fixtureResult.setName(startedEvent.getNode().getName());
            fixtureResult.setParameters(convertStepParameters(startedEvent.getNode().getMethodParameters()));
            lifecycle.startTearDownFixture(TEST_CONTAINER_PREFIX_NAME + startedEvent.getNode().getParentId(),
                    startedEvent.getNode().getRuntimeId().toString(), fixtureResult);
        } else {
            startStep(startedEvent.getNode());
        }
    }

    @EventListener
    public void afterNodeCompleted(SprimberEventPublisher.AfterNodeCompletedEvent completedEvent) {
        if ("test".equals(testParentsById.get(completedEvent.getNode().getParentId().toString()).getParentType())) {
            updateAndStopNonExceptionFixture(completedEvent.getNode());
        } else {
            completeStep(completedEvent.getNode());
        }
    }

    private void updateAndStopNonExceptionFixture(Node node) {
        lifecycle.updateFixture(node.getRuntimeId().toString(),
                fixtureResult -> {
                    if (node.isCompletedSuccessfully()) {
                        fixtureResult.setStatus(Status.PASSED);
                    }
                    if (node.isCompletedWithSkip()) {
                        fixtureResult.setStatus(Status.SKIPPED);
                    }
                });
        lifecycle.stopFixture(node.getRuntimeId().toString());
    }

    @EventListener
    public void afterNodeError(SprimberEventPublisher.AfterNodeErrorEvent errorEvent) {
        stopStepWithError(errorEvent.getNode());
    }

    private void startStep(Node node) {
        StepResult stepResult = new StepResult();
        stepResult.setName(node.getName());
        stepResult.setParameters(convertStepParameters(node.getMethodParameters()));
        lifecycle.startStep(node.getParentId().toString(), node.getRuntimeId().toString(), stepResult);
        StringBuilder dataTableCsv = new StringBuilder();
        node.attributesStream()
                .filter(entry -> !entry.getKey().equals("stepData"))
                .forEach(entry -> dataTableCsv.append(entry.getKey()).append("\t").append(entry.getValue()).append("\n"));
        if (!dataTableCsv.toString().isEmpty()) {
            lifecycle.addAttachment("Step Attributes", "text/tab-separated-values", "csv", dataTableCsv.toString().getBytes());
        }
        attachStepDataParameterIfPresent(String.valueOf(node.getAttribute("stepData").orElse("")));
    }

    private void completeStep(Node node) {
        lifecycle.updateStep(node.getRuntimeId().toString(),
                stepResult -> {
                    if (node.isCompletedSuccessfully()) {
                        stepResult.setStatus(Status.PASSED);
                    }
                    if (node.isCompletedWithSkip()) {
                        stepResult.setStatus(Status.SKIPPED);
                    }
                }
        );
        lifecycle.stopStep(node.getRuntimeId().toString());
    }

    private void stopStepWithError(Node node) {
        if ("test".equals(testParentsById.get(node.getParentId().toString()).getParentType())) {
            Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(node.getThrowable().get());
            lifecycle.updateFixture(node.getRuntimeId().toString(), fixtureResult -> {
                fixtureResult.setStatus(node.getThrowable().map(this::mapThrowable).orElse(Status.BROKEN));
                statusDetails.ifPresent(fixtureResult::setStatusDetails);
            });
            lifecycle.stopFixture(node.getRuntimeId().toString());
            lifecycle.setCurrentTestCase(node.getParentId().toString());
        } else {
            Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(node.getThrowable().get());
            lifecycle.updateStep(node.getRuntimeId().toString(),
                    stepResult -> {
                        stepResult.setStatus(node.getThrowable().map(this::mapThrowable).orElse(Status.BROKEN));
                        statusDetails.ifPresent(stepResult::setStatusDetails);
                    });
            lifecycle.stopStep(node.getRuntimeId().toString());
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

    private void attachExceptionMessage(Throwable throwable) {
        lifecycle.addAttachment("Exception Message", "text/plain", ".exception", throwable.getLocalizedMessage().getBytes());
    }

    private void attachStepDataParameterIfPresent(String stepData) {
        if (isNotBlank(stepData)) {
            final String attachmentSource = lifecycle
                    .prepareAttachment("Data table", "text/tab-separated-values", "csv");
            lifecycle.writeAttachment(attachmentSource,
                    new ByteArrayInputStream(stepData.getBytes(Charset.forName("UTF-8"))));
        }
    }

    private Status mapThrowable(Throwable throwable) {
        if (nonPrintableExceptions.contains(throwable.getClass().getName())) {
            return Status.FAILED;
        }
        return Status.BROKEN;
    }

    @Data
    private class TestParentInfo {
        private String parentName;
        private String parentType;
    }
}
