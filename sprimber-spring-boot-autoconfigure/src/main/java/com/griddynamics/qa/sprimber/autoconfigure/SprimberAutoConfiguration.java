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

import com.griddynamics.qa.sprimber.engine.model.configuration.SprimberProperties;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static com.griddynamics.qa.sprimber.engine.model.ThreadConstants.SPRIMBER_TC_EXECUTOR_NAME;
import static com.griddynamics.qa.sprimber.engine.model.ThreadConstants.SPRIMBER_TS_EXECUTOR_NAME;

/**
 * @author fparamonov
 */
@Configuration
@ComponentScan(basePackages = {"com.griddynamics.qa.sprimber.engine", "com.griddynamics.qa.sprimber.lifecycle"})
@EnableAspectJAutoProxy
@Import(SprimberBeans.class)
public class SprimberAutoConfiguration {

    @Bean(SPRIMBER_TC_EXECUTOR_NAME)
    public Executor asyncTcExecutor(SprimberProperties sprimberProperties) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(sprimberProperties.getTcExecutorPool().getCoreSize());
        taskExecutor.setMaxPoolSize(sprimberProperties.getTcExecutorPool().getMaxSIze());
        taskExecutor.setKeepAliveSeconds(sprimberProperties.getTcExecutorPool().getKeepAliveSeconds());
        taskExecutor.setThreadNamePrefix(sprimberProperties.getTcExecutorPool().getThreadNamePrefix());
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean(SPRIMBER_TS_EXECUTOR_NAME)
    public Executor asyncTsExecutor(SprimberProperties sprimberProperties) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(sprimberProperties.getTsExecutorPool().getCoreSize());
        taskExecutor.setMaxPoolSize(sprimberProperties.getTsExecutorPool().getMaxSIze());
        taskExecutor.setKeepAliveSeconds(sprimberProperties.getTsExecutorPool().getKeepAliveSeconds());
        taskExecutor.setThreadNamePrefix(sprimberProperties.getTsExecutorPool().getThreadNamePrefix());
        taskExecutor.initialize();
        return taskExecutor;
    }
}
