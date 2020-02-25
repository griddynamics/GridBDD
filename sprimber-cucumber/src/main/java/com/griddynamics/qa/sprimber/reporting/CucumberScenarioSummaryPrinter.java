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

package com.griddynamics.qa.sprimber.reporting;

import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.engine.TreeExecutorContext;
import com.griddynamics.qa.sprimber.runtime.ExecutionContext;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

import static com.griddynamics.qa.sprimber.discovery.CucumberAdapterConstants.*;

/**
 * @author fparamonov
 */

public class CucumberScenarioSummaryPrinter extends TestSummaryPrinter {

    private final List<String> testRoles = new ArrayList<>();

    public CucumberScenarioSummaryPrinter(ExecutionContext executionContext, TreeExecutorContext treeExecutorContext) {
        super(executionContext, treeExecutorContext);
        testRoles.add(BEFORE_TEST_ACTION_STYLE);
        testRoles.add(BEFORE_STEP_ACTION_STYLE);
        testRoles.add(AFTER_STEP_ACTION_STYLE);
        testRoles.add(AFTER_TEST_ACTION_STYLE);
        testRoles.add(CUCUMBER_STEP_ROLE);
        testRoles.add(CUCUMBER_STEP_CONTAINER_ROLE);
    }

    @Override
    boolean isNodeLevelAboveTest(Node node) {
        return !testRoles.contains(node.getRole());
    }

    @Override
    String getTestStageName() {
        return CUCUMBER_SCENARIO_ROLE;
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.CucumberAdapterConstants).ADAPTER_NAME")
    public void containerNodeStarted(SprimberEventPublisher.ContainerNodeStartedEvent startedEvent) {
        increaseDepthLevel(startedEvent.getNode());
        if (CUCUMBER_SCENARIO_ROLE.equals(startedEvent.getNode().getRole())) {
            doWithTestStart(startedEvent.getNode());
        }
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.CucumberAdapterConstants).ADAPTER_NAME")
    public void containerNodeFinished(SprimberEventPublisher.ContainerNodeFinishedEvent finishedEvent) {
        decreaseDepthLevel(finishedEvent.getNode());
        if (CUCUMBER_SCENARIO_ROLE.equals(finishedEvent.getNode().getRole())) {
            doWithTestFinish(finishedEvent.getNode());
        }
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.CucumberAdapterConstants).ADAPTER_NAME")
    public void invokableNodeStarted(SprimberEventPublisher.InvokableNodeStartedEvent startedEvent) {
        increaseDepthLevel(startedEvent.getNode());
        if (CUCUMBER_STEP_ROLE.equals(startedEvent.getNode().getRole())) {
            increaseDepthLevel(startedEvent.getNode());
        }
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.CucumberAdapterConstants).ADAPTER_NAME")
    public void invokableNodeFinished(SprimberEventPublisher.InvokableNodeFinishedEvent finishedEvent) {
        addSuccessNodeRow(finishedEvent.getNode());
        decreaseDepthLevel(finishedEvent.getNode());
        if (CUCUMBER_STEP_ROLE.equals(finishedEvent.getNode().getRole())) {
            decreaseDepthLevel(finishedEvent.getNode());
        }
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.CucumberAdapterConstants).ADAPTER_NAME")
    public void invokableNodeError(SprimberEventPublisher.InvokableNodeErrorEvent errorEvent) {
        addErrorNodeRow(errorEvent.getNode());
        decreaseDepthLevel(errorEvent.getNode());
        if (CUCUMBER_STEP_ROLE.equals(errorEvent.getNode().getRole())) {
            decreaseDepthLevel(errorEvent.getNode());
        }
    }
}
