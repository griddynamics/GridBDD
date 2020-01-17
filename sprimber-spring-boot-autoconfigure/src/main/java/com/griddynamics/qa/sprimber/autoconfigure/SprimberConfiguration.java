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

import com.griddynamics.qa.sprimber.configuration.SprimberProperties;
import com.griddynamics.qa.sprimber.engine.EngineSpringConfiguration;
import com.griddynamics.qa.sprimber.reporting.SprimberEventPublisher;
import com.griddynamics.qa.sprimber.runtime.RuntimeConfiguration;
import com.griddynamics.qa.sprimber.scope.FlowOrchestrator;
import com.griddynamics.qa.sprimber.scope.TestCaseScope;
import com.griddynamics.qa.sprimber.stepdefinition.StepDefinitionSrpingConfiguration;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.util.concurrent.Executor;

import static com.griddynamics.qa.sprimber.scope.TestCaseScope.TEST_CASE_SCOPE_NAME;

/**
 * @author fparamonov
 */

@Configuration
@EnableConfigurationProperties(SprimberProperties.class)
public class SprimberConfiguration {

    @Bean
    @ConditionalOnProperty(value = "sprimber.configuration.custom.scopes.enable", havingValue = "true", matchIfMissing = true)
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return beanFactory -> {
            TestCaseScope testCaseScope = new TestCaseScope();
            beanFactory.registerScope(TEST_CASE_SCOPE_NAME, testCaseScope);
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Configuration
    static class SprimberExecutors {

        @Deprecated
        @Bean("sprimber-executor")
        public Executor asyncExecutor() {
            return getDefaultExecutor("TCExecutor-");
        }

        @Bean("testExecutor")
        @ConditionalOnMissingBean(name = "testExecutor")
        public Executor testExecutor() {
            return getDefaultExecutor("TestExecutor-");
        }

        @Bean("node-async-pool")
        @ConditionalOnMissingBean(name = "node-async-pool")
        public Executor nodeAsyncPool() {
            return getDefaultExecutor("NodeAsyncPool");
        }

        private ThreadPoolTaskExecutor getDefaultExecutor(String s) {
            ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
            taskExecutor.setCorePoolSize(3);
            taskExecutor.setMaxPoolSize(3);
            taskExecutor.setThreadNamePrefix(s);
            taskExecutor.initialize();
            return taskExecutor;
        }
    }

    @Configuration
    @Import({EngineSpringConfiguration.class, StepDefinitionSrpingConfiguration.class, RuntimeConfiguration.class,
            FlowOrchestrator.class, SprimberEventPublisher.class})
    static class SprimberCore {
    }
}
