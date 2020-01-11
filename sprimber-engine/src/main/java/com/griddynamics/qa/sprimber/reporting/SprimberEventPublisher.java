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
    public void stageStarted(Node node) {
        ContainerNodeStartedEvent containerNodeStartedEvent = new ContainerNodeStartedEvent(this, (Node) node);
        eventPublisher.publishEvent(containerNodeStartedEvent);
    }

    @Override
    public void stageFinished(Node node) {
        ContainerNodeFinishedEvent containerNodeFinishedEvent = new ContainerNodeFinishedEvent(this, (Node) node);
        eventPublisher.publishEvent(containerNodeFinishedEvent);
    }

    @Override
    public void beforeNodeStarted(Node node) {
        BeforeNodeStartedEvent beforeNodeStartedEvent = new BeforeNodeStartedEvent(this, (Node) node);
        eventPublisher.publishEvent(beforeNodeStartedEvent);
    }

    @Override
    public void beforeNodeCompleted(Node node) {
        BeforeNodeCompletedEvent beforeNodeCompletedEvent = new BeforeNodeCompletedEvent(this, (Node) node);
        eventPublisher.publishEvent(beforeNodeCompletedEvent);
    }

    @Override
    public void beforeNodeError(Node node) {
        BeforeNodeErrorEvent beforeNodeErrorEvent = new BeforeNodeErrorEvent(this, (Node) node);
        eventPublisher.publishEvent(beforeNodeErrorEvent);
    }

    @Override
    public void targetNodeStarted(Node node) {
        TargetNodeStartedEvent targetNodeStartedEvent = new TargetNodeStartedEvent(this, (Node) node);
        eventPublisher.publishEvent(targetNodeStartedEvent);
    }

    @Override
    public void targetNodeCompleted(Node node) {
        TargetNodeCompletedEvent targetNodeFinishedEvent = new TargetNodeCompletedEvent(this, (Node) node);
        eventPublisher.publishEvent(targetNodeFinishedEvent);
    }

    @Override
    public void targetNodeError(Node node) {
        TargetNodeErrorEvent targetNodeFinishedEvent = new TargetNodeErrorEvent(this, (Node) node);
        eventPublisher.publishEvent(targetNodeFinishedEvent);
    }

    @Override
    public void afterNodeStarted(Node node) {
        AfterNodeStartedEvent afterNodeStartedEvent = new AfterNodeStartedEvent(this, (Node) node);
        eventPublisher.publishEvent(afterNodeStartedEvent);
    }

    @Override
    public void afterNodeCompleted(Node node) {
        AfterNodeCompletedEvent afterNodeCompletedEvent = new AfterNodeCompletedEvent(this, (Node) node);
        eventPublisher.publishEvent(afterNodeCompletedEvent);
    }

    @Override
    public void afterNodeError(Node node) {
        AfterNodeErrorEvent afterNodeErrorEvent = new AfterNodeErrorEvent(this, (Node) node);
        eventPublisher.publishEvent(afterNodeErrorEvent);
    }

    public class ContainerNodeStartedEvent extends Events.NodeEvent {

        public ContainerNodeStartedEvent(Object source) {
            super(source);
        }

        public ContainerNodeStartedEvent(Object source, Node containerNode) {
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

    // Target node events

    public class TargetNodeStartedEvent extends Events.NodeEvent {

        public TargetNodeStartedEvent(Object source) {
            super(source);
        }

        public TargetNodeStartedEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    public class TargetNodeCompletedEvent extends Events.NodeEvent {

        public TargetNodeCompletedEvent(Object source) {
            super(source);
        }

        public TargetNodeCompletedEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    public class TargetNodeErrorEvent extends Events.NodeEvent {

        public TargetNodeErrorEvent(Object source) {
            super(source);
        }

        public TargetNodeErrorEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    //Before node events

    public class BeforeNodeStartedEvent extends Events.NodeEvent {

        public BeforeNodeStartedEvent(Object source) {
            super(source);
        }

        public BeforeNodeStartedEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    public class BeforeNodeCompletedEvent extends Events.NodeEvent {

        public BeforeNodeCompletedEvent(Object source) {
            super(source);
        }

        public BeforeNodeCompletedEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    public class BeforeNodeErrorEvent extends Events.NodeEvent {

        public BeforeNodeErrorEvent(Object source) {
            super(source);
        }

        public BeforeNodeErrorEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    // After node events

    public class AfterNodeStartedEvent extends Events.NodeEvent {

        public AfterNodeStartedEvent(Object source) {
            super(source);
        }

        public AfterNodeStartedEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    public class AfterNodeCompletedEvent extends Events.NodeEvent {

        public AfterNodeCompletedEvent(Object source) {
            super(source);
        }

        public AfterNodeCompletedEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }

    public class AfterNodeErrorEvent extends Events.NodeEvent {

        public AfterNodeErrorEvent(Object source) {
            super(source);
        }

        public AfterNodeErrorEvent(Object source, Node executableNode) {
            super(source, executableNode);
        }
    }
}
