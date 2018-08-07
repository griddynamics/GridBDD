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

import com.griddynamics.qa.sprimber.engine.processor.cucumber.JacksonDataTableTransformer;
import cucumber.api.Pending;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import io.cucumber.stepexpression.StepExpressionFactory;
import io.cucumber.stepexpression.TypeRegistry;
import io.qameta.allure.AllureLifecycle;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public List<String> skippedExceptionNames() {
        List<String> skippedExceptions = new ArrayList<>();
        skippedExceptions.add("org.junit.AssumptionViolatedException");
        skippedExceptions.add("org.junit.internal.AssumptionViolatedException");
        skippedExceptions.add("org.testng.SkipException");
        return skippedExceptions;
    }

    @Bean
    public List<Class<? extends Annotation>> pendingExceptionAnnotations() {
        List<Class<? extends Annotation>> pendingAnnotations = new ArrayList<>();
        pendingAnnotations.add(Pending.class);
        return pendingAnnotations;
    }
}
