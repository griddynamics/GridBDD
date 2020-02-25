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

import static com.griddynamics.qa.sprimber.discovery.ClassicAdapterConstants.CLASSIC_TEST_ROLE;

/**
 * @author fparamonov
 */
public class ClassicTestSummaryPrinter extends TestSummaryPrinter {

    public ClassicTestSummaryPrinter(ExecutionContext executionContext, TreeExecutorContext context) {
        super(executionContext, context);
    }

    @Override
    boolean isNodeLevelAboveTest(Node node) {
        return !CLASSIC_TEST_ROLE.equals(node.getRole()); //Since there are no hooks and steps for now
    }

    @Override
    String getTestStageName() {
        return CLASSIC_TEST_ROLE;
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.ClassicAdapterConstants).CLASSIC_ADAPTER_NAME")
    public void containerNodeStarted(SprimberEventPublisher.ContainerNodeStartedEvent startedEvent) {
        increaseDepthLevel(startedEvent.getNode());
        if (CLASSIC_TEST_ROLE.equals(startedEvent.getNode().getRole())) {
            doWithTestStart(startedEvent.getNode());
        }
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.ClassicAdapterConstants).CLASSIC_ADAPTER_NAME")
    public void containerNodeFinished(SprimberEventPublisher.ContainerNodeFinishedEvent finishedEvent) {
        decreaseDepthLevel(finishedEvent.getNode());
        if (CLASSIC_TEST_ROLE.equals(finishedEvent.getNode().getRole())) {
            doWithTestFinish(finishedEvent.getNode());
        }
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.ClassicAdapterConstants).CLASSIC_ADAPTER_NAME",
            classes = {SprimberEventPublisher.InvokableNodeStartedEvent.class})
    public void invokableNodeStarted(SprimberEventPublisher.InvokableNodeStartedEvent startedEvent) {
        increaseDepthLevel(startedEvent.getNode());
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.ClassicAdapterConstants).CLASSIC_ADAPTER_NAME")
    public void invokableNodeFinished(SprimberEventPublisher.InvokableNodeFinishedEvent finishedEvent) {
        addSuccessNodeRow(finishedEvent.getNode());
        decreaseDepthLevel(finishedEvent.getNode());
    }

    @EventListener(condition = "#root.event.node.adapterName == T(com.griddynamics.qa.sprimber.discovery.ClassicAdapterConstants).CLASSIC_ADAPTER_NAME")
    public void invokableNodeError(SprimberEventPublisher.InvokableNodeErrorEvent errorEvent) {
        addErrorNodeRow(errorEvent.getNode());
        decreaseDepthLevel(errorEvent.getNode());
    }
}
