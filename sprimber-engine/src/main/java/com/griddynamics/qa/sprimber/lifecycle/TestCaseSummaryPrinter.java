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

package com.griddynamics.qa.sprimber.lifecycle;

import com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase.TestCaseFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase.TestCaseStartedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.testhook.TestHookFinishedEvent;
import com.griddynamics.qa.sprimber.lifecycle.model.executor.teststep.TestStepFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.NamedThreadLocal;

import java.util.Arrays;

/**
 * This is a helper class that able to print shor summary after each scenario to info log level
 * By default this bean not configured automatically,
 * use property {@code sprimber.configuration.summary.printer.enable} to enable it
 *
 * @author fparamonov
 */

public class TestCaseSummaryPrinter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseSummaryPrinter.class);
    private ThreadLocal<StringBuilder> reportBuilder = new NamedThreadLocal<>("Testcase report builder");

    @EventListener
    public void illuminateTestCaseStart(TestCaseStartedEvent startEvent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n");
        stringBuilder.append(String.format("Test Case Started: %s", startEvent.getTestCase().getName()));
        reportBuilder.set(stringBuilder);
    }

    @EventListener
    public void illuminateTestCaseFinish(TestCaseFinishedEvent finishEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n\n");
        LOGGER.info(stringBuilder.toString());
        reportBuilder.remove();
    }

    @EventListener
    public void illuminateTestStepFinish(TestStepFinishedEvent finishEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(String.format(" %s", finishEvent.getTestStep().getStepAction().getActionType()));
        stringBuilder.append(String.format(" %s", finishEvent.getTestStep().getActualText()));
        stringBuilder.append(String.format(" %s", Arrays.toString(finishEvent.getTestStep().getStepArguments())));
        stringBuilder.append(String.format(" (%s) ", finishEvent.getExecutionResult()));
    }

    @EventListener
    public void illuminateTestHookFinish(TestHookFinishedEvent finishEvent) {
        StringBuilder stringBuilder = reportBuilder.get();
        stringBuilder.append("\n");
        stringBuilder.append(finishEvent.getHookDefinition().getActionType());
    }
}
