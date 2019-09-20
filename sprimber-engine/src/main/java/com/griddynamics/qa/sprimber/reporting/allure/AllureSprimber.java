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

package com.griddynamics.qa.sprimber.reporting.allure;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuite;
import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import com.griddynamics.qa.sprimber.reporting.StepDefinitionFormatter;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.model.*;
import io.qameta.allure.util.ResultsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author fparamonov
 */

@Slf4j
@Component
public class AllureSprimber {

    private static final String CUCUMBER_SUITES = "Cucumber Suites";
    private static final int DEFAULT_AWAIT_TERMINATION_SECONDS = 5;
    private static final String ALLURE_WRITER_POOL_PREFIX = "AllureWriter-";
    private static final String SUB_CONTAINER_PREFIX_NAME = "subContainer-";
    private static final String EPIC_DEFAULT_NAME = "Cucumber Features";
    private final Map<ExecutionResult.Status, Status> allureToSprimberStatusMapping;

    private final AllureLifecycle lifecycle;
    private ThreadPoolTaskExecutor taskExecutor;
    private StepDefinitionFormatter formatter;

    public AllureSprimber(Map<ExecutionResult.Status, Status> allureToSprimberStatusMapping,
                          AllureLifecycle lifecycle, StepDefinitionFormatter formatter) {
        this.allureToSprimberStatusMapping = allureToSprimberStatusMapping;
        this.lifecycle = lifecycle;
        this.formatter = formatter;
        initThreadPool();
    }

    private void initThreadPool() {
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(ALLURE_WRITER_POOL_PREFIX);
        taskExecutor.initialize();
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(DEFAULT_AWAIT_TERMINATION_SECONDS);
    }

    @PreDestroy
    public void destroy() {
        taskExecutor.destroy();
    }

    @EventListener
    public void testCaseStarted(SprimberEventPublisher.TestCaseStartedEvent testCaseStartedEvent) {
        TestResultContainer testCaseResultContainer = new TestResultContainer();
        testCaseResultContainer.setName(testCaseStartedEvent.getTestCase().getName());
        testCaseResultContainer.setDescription(testCaseStartedEvent.getTestCase().getDescription());
        testCaseResultContainer.setUuid(testCaseStartedEvent.getTestCase().getRuntimeId());
        lifecycle.startTestContainer(testCaseResultContainer);
        testCaseStartedEvent.getTestCase().getTests().forEach(test -> {
            scheduleTest(testCaseStartedEvent, test);
        });
    }

    private void scheduleTest(SprimberEventPublisher.TestCaseStartedEvent testCaseStartedEvent, TestSuite.Test test) {
        TestResultContainer testResultContainer = new TestResultContainer();
        testResultContainer.setUuid(SUB_CONTAINER_PREFIX_NAME + test.getRuntimeId());
        lifecycle.startTestContainer(testCaseStartedEvent.getTestCase().getRuntimeId(), testResultContainer);

        TestResult testResult = prepareTestResult(testCaseStartedEvent, test);

        lifecycle.scheduleTestCase(testResultContainer.getUuid(), testResult);
    }

    private TestResult prepareTestResult(SprimberEventPublisher.TestCaseStartedEvent testCaseStartedEvent, TestSuite.Test test) {
        TestResult testResult = new TestResult();
        testResult.setName(test.getName());
        testResult.setDescription(test.getDescription());
        testResult.setUuid(test.getRuntimeId());
        testResult.setHistoryId(test.getHistoryId());
        List<Label> labels = processTestLabels(testCaseStartedEvent, test);
        List<Link> links = processTestLinks(testCaseStartedEvent, test);
        testResult.setLabels(labels);
        testResult.setLinks(links);
        return testResult;
    }

    private List<Link> processTestLinks(SprimberEventPublisher.TestCaseStartedEvent testCaseStartedEvent, TestSuite.Test test) {
        return Collections.emptyList();
    }

    private List<Label> processTestLabels(SprimberEventPublisher.TestCaseStartedEvent testCaseStartedEvent, TestSuite.Test test) {
        List<Label> labels = new ArrayList<>();
        Label epicLabel = ResultsUtils.createEpicLabel(test.getMeta().getSingleValueOrDefault("epic", EPIC_DEFAULT_NAME));
        Label featureLabel = ResultsUtils.createFeatureLabel(test.getMeta().getSingleValueOrDefault("feature", testCaseStartedEvent.getTestCase().getName()));
        Label storyLabel = ResultsUtils.createStoryLabel(test.getMeta().getSingleValueOrDefault("story", test.getName()));

        Label packageLabel = ResultsUtils.createPackageLabel(test.getMeta().getSingleValueOrDefault("package", testCaseStartedEvent.getTestCase().getName()));

        Label parentSuiteLabel = ResultsUtils.createParentSuiteLabel(test.getMeta().getSingleValueOrDefault("parentSuite", CUCUMBER_SUITES));
        Label suiteLabel = ResultsUtils.createSuiteLabel(test.getMeta().getSingleValueOrDefault("suite", testCaseStartedEvent.getTestCase().getName()));
        Label subSuiteLabel = ResultsUtils.createSubSuiteLabel(test.getMeta().getSingleValueOrDefault("subSuite", test.getName()));

        Label severityLabel = ResultsUtils.createSeverityLabel(test.getMeta().getSingleValueOrDefault("severity", SeverityLevel.NORMAL.value()));

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

    @EventListener
    public void testCaseFinished(SprimberEventPublisher.TestCaseFinishedEvent testCaseFinishedEvent) {
        lifecycle.stopTestContainer(testCaseFinishedEvent.getTestCase().getRuntimeId());
        lifecycle.writeTestContainer(testCaseFinishedEvent.getTestCase().getRuntimeId());
    }

    @EventListener
    public void testStarted(SprimberEventPublisher.TestStartedEvent testStartedEvent) {
        lifecycle.startTestCase(testStartedEvent.getTest().getRuntimeId());
    }

    @EventListener
    public void testFinished(SprimberEventPublisher.TestFinishedEvent testFinishedEvent) {
        ExecutionResult result = testFinishedEvent.getTest().getExecutionResult();
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(result.getOptionalError().orElse(null));
        String runtimeUuid = testFinishedEvent.getTest().getRuntimeId();
        lifecycle.updateTestCase(runtimeUuid, scenarioResult -> {
            scenarioResult.setStatus(allureToSprimberStatusMapping.get(result.getStatus()));
            statusDetails.ifPresent(scenarioResult::setStatusDetails);
        });
        lifecycle.stopTestCase(runtimeUuid);
        lifecycle.stopTestContainer(SUB_CONTAINER_PREFIX_NAME + testFinishedEvent.getTest().getRuntimeId());

        taskExecutor.execute(() -> {
            lifecycle.writeTestCase(runtimeUuid);
            lifecycle.writeTestContainer(SUB_CONTAINER_PREFIX_NAME + testFinishedEvent.getTest().getRuntimeId());
        });

    }

    @EventListener
    public void testStepStarted(SprimberEventPublisher.StepStartedEvent stepStartedEvent) {
        String stepReportName = stepStartedEvent.getStep().getStepDefinition().getStepType() + " " +
                stepStartedEvent.getStep().getStepDefinition().getStepPhase() + " " +
                formatter.formatStepName(stepStartedEvent.getStep());

        if (stepStartedEvent.getStep().isHookStep()
                && stepStartedEvent.getStep().getStepDefinition().getStepPhase().equals(StepDefinition.StepPhase.TEST)) {
            FixtureResult fixtureResult = new FixtureResult();
            fixtureResult.setName(stepReportName);
            fixtureResult.setParameters(convertStepParameters(stepStartedEvent.getStep()));
            if (stepStartedEvent.getStep().getStepDefinition().getStepType().equals(StepDefinition.StepType.BEFORE)) {
                lifecycle.startPrepareFixture(SUB_CONTAINER_PREFIX_NAME + stepStartedEvent.getStep().getParentId(),
                        stepStartedEvent.getStep().getRuntimeId(), fixtureResult);
            }
            if (stepStartedEvent.getStep().getStepDefinition().getStepType().equals(StepDefinition.StepType.AFTER)) {
                lifecycle.startTearDownFixture(SUB_CONTAINER_PREFIX_NAME + stepStartedEvent.getStep().getParentId(),
                        stepStartedEvent.getStep().getRuntimeId(), fixtureResult);
            }
        } else {
            StepResult stepResult = new StepResult();
            stepResult.setName(stepReportName);
            stepResult.setParameters(convertStepParameters(stepStartedEvent.getStep()));
            lifecycle.startStep(stepStartedEvent.getStep().getParentId(), stepStartedEvent.getStep().getRuntimeId(), stepResult);
        }

        StringBuilder dataTableCsv = new StringBuilder();
        stepStartedEvent.getStep().getStepDefinition().getAttributes().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("stepData"))
                .forEach(entry -> dataTableCsv.append(entry.getKey()).append("\t").append(entry.getValue()).append("\n"));

        lifecycle.addAttachment("Step Attributes", "text/tab-separated-values", "csv", dataTableCsv.toString().getBytes());
        attachStepDataParameterIfPresent(String.valueOf(stepStartedEvent.getStep().getStepDefinition().getAttributes().getOrDefault("stepData", "")));
    }

    @EventListener
    public void testStepFinished(SprimberEventPublisher.StepFinishedEvent stepFinishedEvent) {
        ExecutionResult result = stepFinishedEvent.getStep().getExecutionResult();
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(result.getOptionalError().orElse(null));

        if (stepFinishedEvent.getStep().isHookStep()
                && stepFinishedEvent.getStep().getStepDefinition().getStepPhase().equals(StepDefinition.StepPhase.TEST)) {
            lifecycle.updateFixture(stepFinishedEvent.getStep().getRuntimeId(), fixtureResult -> {
                fixtureResult.setStatus(allureToSprimberStatusMapping.get(result.getStatus()));
                statusDetails.ifPresent(fixtureResult::setStatusDetails);
            });
            lifecycle.stopFixture(stepFinishedEvent.getStep().getRuntimeId());
        } else {
            lifecycle.updateStep(stepFinishedEvent.getStep().getRuntimeId(),
                    stepResult -> {
                        stepResult.setStatus(allureToSprimberStatusMapping.get(result.getStatus()));
                        statusDetails.ifPresent(stepResult::setStatusDetails);
                    }
            );
            lifecycle.stopStep(stepFinishedEvent.getStep().getRuntimeId());
        }

    }

    private List<Parameter> convertStepParameters(TestSuite.Step step) {
        return step.getParameters().entrySet().stream()
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
}
