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

import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.processor.cucumber.JacksonDataTableTransformer;
import com.griddynamics.qa.sprimber.engine.scope.TestCaseScope;
import com.griddynamics.qa.sprimber.reporting.TestCaseSummaryPrinter;
import cucumber.api.Pending;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import io.cucumber.stepexpression.StepExpressionFactory;
import io.cucumber.stepexpression.TypeRegistry;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.util.*;

import static com.griddynamics.qa.sprimber.engine.scope.TestCaseScope.TEST_CASE_SCOPE_NAME;

/**
 * @author fparamonov
 */
@Configuration
public class SprimberBeans {

    @Bean
    public Parser<GherkinDocument> gherkinParser() {
        return new Parser<>(new AstBuilder());
    }

    @Bean
    public TokenMatcher tokenMatcher() {
        return new TokenMatcher();
    }

    @Bean
    public Compiler compiler() {
        return new Compiler();
    }

    @Bean
    @ConditionalOnMissingBean
    public AllureLifecycle allureLifecycle() {
        return new AllureLifecycle();
    }

    @Bean
    @ConditionalOnMissingBean
    public StepExpressionFactory stepExpressionFactory(TypeRegistry typeRegistry) {
        return new StepExpressionFactory(typeRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeRegistry typeRegistry(JacksonDataTableTransformer jacksonDataTableTransformer) {
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        typeRegistry.dataTableTypeRegistry().setDefaultDataTableEntryTransformer(jacksonDataTableTransformer);
        typeRegistry.dataTableTypeRegistry().setDefaultDataTableCellTransformer(jacksonDataTableTransformer);
        return typeRegistry;
    }

    @Bean
    @ConditionalOnProperty(value = "sprimber.configuration.custom.scopes.enable", matchIfMissing = true)
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return beanFactory -> {
            TestCaseScope testCaseScope = new TestCaseScope();
            beanFactory.registerScope(TEST_CASE_SCOPE_NAME, testCaseScope);
        };
    }

    @Bean
    @ConditionalOnProperty(value = "sprimber.configuration.summary.printer.enable", havingValue = "true")
    public TestCaseSummaryPrinter summaryPrinter() {
        return new TestCaseSummaryPrinter();
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public Map<ExecutionResult.Status, Status> allureToSprimberStatusMapping() {
        HashMap<ExecutionResult.Status, Status> statusMapping = new HashMap<>();
        statusMapping.put(ExecutionResult.Status.PASSED, Status.PASSED);
        statusMapping.put(ExecutionResult.Status.SKIPPED, Status.SKIPPED);
        statusMapping.put(ExecutionResult.Status.FAILED, Status.FAILED);
        statusMapping.put(ExecutionResult.Status.PENDING, Status.BROKEN);
        statusMapping.put(ExecutionResult.Status.BROKEN, Status.BROKEN);
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
