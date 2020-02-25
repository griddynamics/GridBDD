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
import com.griddynamics.qa.sprimber.engine.NodeExecutionEventsPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor
public class SprimberEventPublisher implements NodeExecutionEventsPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void stageStarted(Node parentNode) {
        ContainerNodeStartedEvent containerNodeStartedEvent = new ContainerNodeStartedEvent(this,  parentNode);
        eventPublisher.publishEvent(containerNodeStartedEvent);
    }

    @Override
    public void stageFinished(Node parentNode) {
        ContainerNodeFinishedEvent containerNodeFinishedEvent = new ContainerNodeFinishedEvent(this,  parentNode);
        eventPublisher.publishEvent(containerNodeFinishedEvent);
    }

    @Override
    public void stageError(Node parentNode) {
        ContainerNodeErrorEvent containerNodeErrorEvent = new ContainerNodeErrorEvent(this,  parentNode);
        eventPublisher.publishEvent(containerNodeErrorEvent);
    }

    @Override
    public void nodeStarted(Node node) {
        InvokableNodeStartedEvent invokableNodeStartedEvent = new InvokableNodeStartedEvent(this, node);
        eventPublisher.publishEvent(invokableNodeStartedEvent);
    }

    @Override
    public void nodeFinished(Node node) {
        InvokableNodeFinishedEvent invokableNodeFinishedEvent = new InvokableNodeFinishedEvent(this, node);
        eventPublisher.publishEvent(invokableNodeFinishedEvent);
    }

    @Override
    public void nodeError(Node node) {
        InvokableNodeErrorEvent invokableNodeErrorEvent = new InvokableNodeErrorEvent(this, node);
        eventPublisher.publishEvent(invokableNodeErrorEvent);
    }

    public class ContainerNodeStartedEvent extends Events.NodeEvent {

        public ContainerNodeStartedEvent(Object source) {
            super(source);
        }

        public ContainerNodeStartedEvent(Object source, Node containerNode) {
            super(source, containerNode);
        }
    }

    public class ContainerNodeErrorEvent extends Events.NodeEvent {

        public ContainerNodeErrorEvent(Object source) {
            super(source);
        }

        public ContainerNodeErrorEvent(Object source, Node containerNode) {
            super(source, containerNode);
        }
    }

    public class ContainerNodeFinishedEvent extends Events.NodeEvent {

        public ContainerNodeFinishedEvent(Object source) {
            super(source);
        }

        public ContainerNodeFinishedEvent(Object source, Node containerNode) {
            super(source, containerNode);
        }
    }

    public class InvokableNodeStartedEvent extends Events.NodeEvent {

        public InvokableNodeStartedEvent(Object source) {
            super(source);
        }

        public InvokableNodeStartedEvent(Object source, Node containerNode) {
            super(source, containerNode);
        }
    }

    public class InvokableNodeFinishedEvent extends Events.NodeEvent {

        public InvokableNodeFinishedEvent(Object source) {
            super(source);
        }

        public InvokableNodeFinishedEvent(Object source, Node containerNode) {
            super(source, containerNode);
        }
    }

    public class InvokableNodeErrorEvent extends Events.NodeEvent {

        public InvokableNodeErrorEvent(Object source) {
            super(source);
        }

        public InvokableNodeErrorEvent(Object source, Node containerNode) {
            super(source, containerNode);
        }
    }
}
