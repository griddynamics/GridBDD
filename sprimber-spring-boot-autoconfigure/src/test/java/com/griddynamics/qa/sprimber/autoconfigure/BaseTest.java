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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.qa.sprimber.autoconfigure.annotation.EnableSprimber;
import com.griddynamics.qa.sprimber.engine.executor.CliRunner;
import com.griddynamics.qa.sprimber.engine.model.action.ActionsContainer;
import com.griddynamics.qa.sprimber.engine.configuration.SprimberProperties;
import com.griddynamics.qa.sprimber.engine.processor.cucumber.JacksonDataTableTransformer;
import com.griddynamics.qa.sprimber.engine.scope.FlowOrchestrator;
import com.griddynamics.qa.sprimber.reporting.TestCaseSummaryPrinter;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.pickles.Compiler;
import io.cucumber.stepexpression.StepExpressionFactory;
import io.cucumber.stepexpression.TypeRegistry;
import io.qameta.allure.AllureLifecycle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;

import static com.griddynamics.qa.sprimber.autoconfigure.SprimberRegistrar.ATTRIBUTES_BEAN_NAME;
import static com.griddynamics.qa.sprimber.engine.model.ThreadConstants.SPRIMBER_EXECUTOR_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fparamonov
 */
@RunWith(SpringRunner.class)
public class BaseTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SprimberAutoConfiguration.class));

    @Test
    public void testMainBeans() {
        contextRunner.withUserConfiguration(SomeTestConfiguration.class)
                .withPropertyValues("sprimber.configuration.summary.printer.enable=true")
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(CliRunner.class);
                            assertThat(context).hasSingleBean(Parser.class);
                            assertThat(context).hasSingleBean(TokenMatcher.class);
                            assertThat(context).hasSingleBean(Compiler.class);
                            assertThat(context).hasSingleBean(SprimberProperties.class);
                            assertThat(context).hasSingleBean(ActionsContainer.class);
                            assertThat(context).hasSingleBean(AllureLifecycle.class);
                            assertThat(context).hasSingleBean(Clock.class);
                            assertThat(context).hasSingleBean(StepExpressionFactory.class);
                            assertThat(context).hasSingleBean(TypeRegistry.class);
                            assertThat(context).hasSingleBean(JacksonDataTableTransformer.class);
                            assertThat(context).hasSingleBean(FlowOrchestrator.class);
                            assertThat(context).hasSingleBean(TestCaseSummaryPrinter.class);
                            assertThat(context).hasBean(SPRIMBER_EXECUTOR_NAME);
                            assertThat(context).hasBean(ATTRIBUTES_BEAN_NAME);
                        }
                );
    }

    @SpringBootApplication
    @EnableSprimber
    static class SomeTestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
