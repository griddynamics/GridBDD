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

import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import com.griddynamics.qa.sprimber.reporting.StepDefinitionFormatter;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.util.ResultsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author fparamonov
 */

@Slf4j
@Component
public class AllureSprimber {

    private static final int DEFAULT_AWAIT_TERMINATION_SECONDS = 5;
    private final Map<ExecutionResult.Status, Status> allureToSprimberStatusMapping;

    private final Clock clock;
    private final AllureLifecycle lifecycle;
    private ThreadPoolTaskExecutor taskExecutor;
    private StepDefinitionFormatter formatter;
    private ThreadLocal<String> testCaseRuntimeId = new ThreadLocal<>();

    public AllureSprimber(Map<ExecutionResult.Status, Status> allureToSprimberStatusMapping,
                          Clock clock,
                          AllureLifecycle lifecycle, StepDefinitionFormatter formatter) {
        this.allureToSprimberStatusMapping = allureToSprimberStatusMapping;
        this.clock = clock;
        this.lifecycle = lifecycle;
        this.formatter = formatter;
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("AllureWriter-");
        taskExecutor.initialize();
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(DEFAULT_AWAIT_TERMINATION_SECONDS);
    }

    @PreDestroy
    public void destroy() {
        taskExecutor.destroy();
    }

    @EventListener
    public void testStarted(SprimberEventPublisher.TestStartedEvent testStartedEvent) {
        testCaseRuntimeId.set(testStartedEvent.getTest().getRuntimeId());
        TestResult testResult = new TestResult()
                .withUuid(testCaseRuntimeId.get())
                .withHistoryId(testStartedEvent.getTest().getHistoryId())
                .withName(testStartedEvent.getTest().getName())
                .withDescription(testStartedEvent.getTest().getDescription());
        lifecycle.scheduleTestCase(testResult);
        lifecycle.startTestCase(testCaseRuntimeId.get());
    }

    @EventListener
    public void testFinished(SprimberEventPublisher.TestFinishedEvent testFinishedEvent) {
        ExecutionResult result = testFinishedEvent.getTest().getExecutionResult();
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(result.getOptionalError().orElse(null));
        String runtimeUuid = testCaseRuntimeId.get();
        lifecycle.updateTestCase(runtimeUuid, scenarioResult -> {
            scenarioResult.withStatus(allureToSprimberStatusMapping.get(result.getStatus()));
            statusDetails.ifPresent(scenarioResult::withStatusDetails);
        });
        lifecycle.stopTestCase(runtimeUuid);
        taskExecutor.execute(() -> lifecycle.writeTestCase(runtimeUuid));
    }

//    @Deprecated
//    @EventListener
//    public void testCaseStarted(TestCaseStartedEvent startedEvent) {
//        HelperInfoBuilder helperInfoBuilder = new HelperInfoBuilder(startedEvent.getTestCase());
//        String historyId = getTestCaseHistoryId(startedEvent.getTestCase());
//        testCaseRuntimeId.set(startedEvent.getTestCase().getRuntimeId());
//        TestResult testResult = new TestResult()
//                .withUuid(testCaseRuntimeId.get())
//                .withHistoryId(historyId)
//                .withName(startedEvent.getTestCase().getName())
//                .withLinks(helperInfoBuilder.getLinks())
//                .withLabels(helperInfoBuilder.getLabels())
//                .withDescription(startedEvent.getTestCase().getDescription());
//        lifecycle.scheduleTestCase(testResult);
//        lifecycle.startTestCase(testCaseRuntimeId.get());
//    }

    @EventListener
    public void testStepStarted(SprimberEventPublisher.StepStartedEvent stepStartedEvent) {
        String stepReportName = stepStartedEvent.getStep().getStepDefinition().getStepType() + " " +
                stepStartedEvent.getStep().getStepDefinition().getStepPhase() + " " +
                formatter.formatStepName(stepStartedEvent.getStep());
        StepResult stepResult = new StepResult()
                .withName(stepReportName)
                .withStart(clock.millis());
        lifecycle.startStep(testCaseRuntimeId.get(), testCaseRuntimeId.get() + stepReportName, stepResult);
        StringBuilder dataTableCsv = new StringBuilder();
        stepStartedEvent.getStep().getStepDefinition().getAttributes().forEach((key, value) ->
                dataTableCsv.append(key).append("\t").append(value).append("\n"));
        lifecycle.addAttachment("Step Attributes", "text/tab-separated-values", "csv", dataTableCsv.toString().getBytes());
        attachStepDataParameterIfPresent(String.valueOf(stepStartedEvent.getStep().getParameters().get("stepData")));
    }

    @EventListener
    public void testStepFinished(SprimberEventPublisher.StepFinishedEvent stepFinishedEvent) {
        String stepReportName = stepFinishedEvent.getStep().getStepDefinition().getStepType() + " " +
                stepFinishedEvent.getStep().getStepDefinition().getStepPhase() + " " +
                formatter.formatStepName(stepFinishedEvent.getStep());
        ExecutionResult result = stepFinishedEvent.getStep().getExecutionResult();
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(result.getOptionalError().orElse(null));
        lifecycle.updateStep(testCaseRuntimeId.get() + stepReportName,
                stepResult -> {
                    stepResult.withStatus(allureToSprimberStatusMapping.get(result.getStatus()));
                    statusDetails.ifPresent(stepResult::withStatusDetails);
                }
        );
        lifecycle.stopStep(testCaseRuntimeId.get() + stepReportName);
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
