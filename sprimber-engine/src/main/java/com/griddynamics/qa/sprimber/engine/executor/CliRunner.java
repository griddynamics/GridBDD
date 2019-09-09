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

package com.griddynamics.qa.sprimber.engine.executor;

import com.griddynamics.qa.sprimber.engine.loader.ActionDefinitionLoader;
import com.griddynamics.qa.sprimber.engine.model.TestCase;
import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.model.action.ActionsContainer;
import com.griddynamics.qa.sprimber.engine.processor.ResourceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

//@Component
public class CliRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliRunner.class);

    private final List<ActionDefinitionLoader> loaders;
    private final List<ResourceProcessor> processors;
    private final TestCaseExecutor testCaseExecutor;
    private final ActionsContainer actionsContainer;

    public CliRunner(List<ActionDefinitionLoader> loaders,
                     List<ResourceProcessor> processors,
                     TestCaseExecutor testCaseExecutor,
                     ActionsContainer actionsContainer) {
        this.loaders = loaders;
        this.processors = processors;
        this.testCaseExecutor = testCaseExecutor;
        this.actionsContainer = actionsContainer;
    }

    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("I'm running from CLI");
        List<ActionDefinition> actionDefinitions =
                loaders.stream().map(ActionDefinitionLoader::load).flatMap(Collection::stream).collect(Collectors.toList());
        actionsContainer.addDefinitions(actionDefinitions);
        List<TestCase> testCases = processors.stream().map(ResourceProcessor::process).flatMap(Collection::stream).collect(Collectors.toList());
        CountDownLatch countDownLatch = new CountDownLatch(testCases.size());
        testCaseExecutor.setCountDownLatch(countDownLatch);
        testCases.forEach(testCaseExecutor::execute);
        countDownLatch.await();
    }
}
