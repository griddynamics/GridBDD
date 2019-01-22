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

package com.griddynamics.qa.sprimber.lifecycle.allure;

import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.model.TestCase;
import com.griddynamics.qa.sprimber.engine.model.TestStep;
import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase.TestCaseFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase.TestCaseStartedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testhook.TestHookFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testhook.TestHookStartedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.teststep.TestStepFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.teststep.TestStepStartedEvent;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.util.ResultsUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author fparamonov
 */
@Component
public class AllureSprimber {

    private static final int DEFAULT_AWAIT_TERMINATION_SECONDS = 5;
    private final Map<ExecutionResult.Status, Status> allureToSprimberStatusMapping;

    private final Clock clock;
    private final AllureLifecycle lifecycle;
    private ThreadPoolTaskExecutor taskExecutor;
    private ThreadLocal<String> testCaseRuntimeId = new ThreadLocal<>();

    public AllureSprimber(Map<ExecutionResult.Status, Status> allureToSprimberStatusMapping,
                          Clock clock,
                          AllureLifecycle lifecycle) {
        this.allureToSprimberStatusMapping = allureToSprimberStatusMapping;
        this.clock = clock;
        this.lifecycle = lifecycle;
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
    public void testCaseStarted(TestCaseStartedEvent startedEvent) {
        HelperInfoBuilder helperInfoBuilder = new HelperInfoBuilder(startedEvent.getTestCase());
        String historyId = getTestCaseHistoryId(startedEvent.getTestCase());
        testCaseRuntimeId.set(startedEvent.getTestCase().getRuntimeId());
        TestResult testResult = new TestResult()
                .withUuid(testCaseRuntimeId.get())
                .withHistoryId(historyId)
                .withName(startedEvent.getTestCase().getName())
                .withLinks(helperInfoBuilder.getLinks())
                .withLabels(helperInfoBuilder.getLabels())
                .withDescription(startedEvent.getTestCase().getDescription());
        lifecycle.scheduleTestCase(testResult);
        lifecycle.startTestCase(testCaseRuntimeId.get());
    }

    @EventListener
    public void testCaseFinished(TestCaseFinishedEvent finishedEvent) throws ExecutionException, InterruptedException, TimeoutException {
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(finishedEvent.getExecutionResult().getOptionalError().orElse(null));
        String runtimeUuid = testCaseRuntimeId.get();
        lifecycle.updateTestCase(runtimeUuid, scenarioResult -> {
            scenarioResult.withStatus(allureToSprimberStatusMapping.get(finishedEvent.getExecutionResult().getStatus()));
            statusDetails.ifPresent(scenarioResult::withStatusDetails);
        });
        lifecycle.stopTestCase(runtimeUuid);
        taskExecutor.execute(() -> lifecycle.writeTestCase(runtimeUuid));
    }

    @EventListener
    public void testHookStarted(TestHookStartedEvent startedEvent) {
        StepResult stepResult = new StepResult()
                .withName(String.valueOf(startedEvent.getHookDefinition().getActionType()) + " " +
                        String.valueOf(startedEvent.getHookDefinition().getActionScope()).toLowerCase())
                .withStart(clock.millis());
        lifecycle.startStep(testCaseRuntimeId.get(), getHookId(startedEvent.getHookDefinition()), stepResult);
    }

    @EventListener
    public void testHookFinished(TestHookFinishedEvent finishedEvent) {
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(finishedEvent.getExecutionResult().getOptionalError().orElse(null));
        lifecycle.updateStep(getHookId(finishedEvent.getHookDefinition()),
                stepResult -> {
                    stepResult.withStatus(allureToSprimberStatusMapping.get(finishedEvent.getExecutionResult().getStatus()));
                    statusDetails.ifPresent(stepResult::withStatusDetails);
                }
        );
        lifecycle.stopStep(getHookId(finishedEvent.getHookDefinition()));
    }

    @EventListener
    public void testStepStarted(TestStepStartedEvent startedEvent) {
        StepResult stepResult = new StepResult()
                .withName(String.format("%s %s", startedEvent.getTestStep().getStepAction().getActionType(), startedEvent.getTestStep().getActualText()))
                .withStart(clock.millis());
        lifecycle.startStep(testCaseRuntimeId.get(), getStepId(startedEvent.getTestStep()), stepResult);

        attachStepDataParameterIfPresent(startedEvent.getTestStep().getStepDataAsText());
    }

    private void attachStepDataParameterIfPresent(String stepData) {
        if (isNotBlank(stepData)) {
            final String attachmentSource = lifecycle
                    .prepareAttachment("Data table", "text/tab-separated-values", "csv");
            lifecycle.writeAttachment(attachmentSource,
                    new ByteArrayInputStream(stepData.getBytes(Charset.forName("UTF-8"))));
        }
    }

    @EventListener
    public void testStepFinished(TestStepFinishedEvent finishedEvent) {
        Optional<StatusDetails> statusDetails = ResultsUtils.getStatusDetails(finishedEvent.getExecutionResult().getOptionalError().orElse(null));
        lifecycle.updateStep(getStepId(finishedEvent.getTestStep()),
                stepResult -> {
                    stepResult.withStatus(allureToSprimberStatusMapping.get(finishedEvent.getExecutionResult().getStatus()));
                    statusDetails.ifPresent(stepResult::withStatusDetails);
                }
        );
        lifecycle.stopStep(getStepId(finishedEvent.getTestStep()));
    }

    private String getStepId(TestStep testStep) {
        return testCaseRuntimeId.get() + testStep.getStepAction().getActionType() + testStep.getActualText();
    }

    private String getHookId(ActionDefinition hookDefinition) {
        return testCaseRuntimeId.get() + hookDefinition.getActionType();
    }

    private String getTestCaseHistoryId(TestCase testCase) {
        try {
            byte[] bytes = MessageDigest.getInstance("md5")
                    .digest((testCase.getParent().getUrl() +
                            testCase.getParent().getName() +
                            testCase.getName() +
                            testCase.getLocation()
                    ).getBytes(UTF_8));
            return new BigInteger(1, bytes).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find md5 hashing algorithm", e);
        }
    }
}
