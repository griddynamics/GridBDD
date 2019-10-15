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

package com.griddynamics.qa.sprimber.autoconfigure;

import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.reporting.StepDefinitionFormatter;
import com.griddynamics.qa.sprimber.reporting.StepExecutionReportCatcher;
import com.griddynamics.qa.sprimber.reporting.TestCaseIlluminator;
import com.griddynamics.qa.sprimber.reporting.TestCaseSummaryPrinter;
import com.griddynamics.qa.sprimber.reporting.allure.AllureSprimber;
import cucumber.api.Pending;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author fparamonov
 */

@Configuration
public class SprimberReportingConfiguration {

    @Configuration
    static class ClassicReporting {
        @Bean
        @ConditionalOnProperty(value = "reporting.summary.enable", prefix = "sprimber.configuration", havingValue = "true", matchIfMissing = true)
        public TestCaseSummaryPrinter summaryPrinter(StepDefinitionFormatter stepDefinitionFormatter) {
            return new TestCaseSummaryPrinter(stepDefinitionFormatter);
        }

        @Bean
        @ConditionalOnProperty(value = "reporting.illuminator.enable", prefix = "sprimber.configuration", havingValue = "true", matchIfMissing = true)
        public TestCaseIlluminator testCaseIlluminator() {
            return new TestCaseIlluminator();
        }

        @Bean
        @ConditionalOnProperty(value = "reporting.aspect.enable", prefix = "sprimber.configuration", havingValue = "true")
        public StepExecutionReportCatcher stepExecutionReportCatcher() {
            return new StepExecutionReportCatcher();
        }

        @Bean
        public StepDefinitionFormatter stepDefinitionFormatter() {
            return new StepDefinitionFormatter();
        }
    }

    @Configuration
    static class AllureReporting {
        @Bean
        public AllureSprimber allureSprimber(Map<Node.Status, Status> allureToSprimberStatusMapping,
                                             AllureLifecycle allureLifecycle, StepDefinitionFormatter stepDefinitionFormatter) {
            return new AllureSprimber(allureToSprimberStatusMapping, allureLifecycle);
        }

        @Bean
        @ConditionalOnMissingBean
        public AllureLifecycle allureLifecycle() {
            return new AllureLifecycle();
        }

        @Bean
        public Map<Node.Status, Status> allureToSprimberStatusMapping() {
            HashMap<Node.Status, Status> statusMapping = new HashMap<>();
            statusMapping.put(Node.Status.COMPLETED, Status.PASSED);
            statusMapping.put(Node.Status.SKIP, Status.SKIPPED);
            statusMapping.put(Node.Status.FAILED, Status.FAILED);
            statusMapping.put(Node.Status.PENDING, Status.BROKEN);
            statusMapping.put(Node.Status.ERROR, Status.BROKEN);
            statusMapping.put(Node.Status.DRY, Status.SKIPPED);
            return statusMapping;
        }

        @Bean
        public Set<String> skippedExceptionNames() {
            Set<String> skippedExceptions = new HashSet<>();
            skippedExceptions.add("org.junit.AssumptionViolatedException");
            skippedExceptions.add("org.junit.internal.AssumptionViolatedException");
            skippedExceptions.add("org.testng.SkipException");
            return skippedExceptions;
        }

        @Bean
        public Set<String> failedExceptionNames() {
            Set<String> failedExceptions = new HashSet<>();
            failedExceptions.add("java.lang.AssertionError");
            return failedExceptions;
        }

        @Bean
        public List<Class<? extends Annotation>> pendingExceptionAnnotations() {
            List<Class<? extends Annotation>> pendingAnnotations = new ArrayList<>();
            pendingAnnotations.add(Pending.class);
            return pendingAnnotations;
        }

    }
}
