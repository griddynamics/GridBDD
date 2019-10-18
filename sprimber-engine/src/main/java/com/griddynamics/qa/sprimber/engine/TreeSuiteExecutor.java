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

package com.griddynamics.qa.sprimber.engine;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.griddynamics.qa.sprimber.engine.Node.*;

/**
 * @author fparamonov
 */

@Slf4j
class TreeSuiteExecutor implements TreeExecutor {

    private static final String STARTED_EVENT_POSTFIX = "Started";
    private static final String COMPLETED_EVENT_POSTFIX = "Completed";
    private static final String ERROR_EVENT_POSTFIX = "Error";
    private static final String EXECUTOR_NAME_SUFFIX = "Executor";
    private final NodeInvoker nodeInvoker;
    private final NodeFallbackManager nodeFallbackManager;
    private final NodeExecutionEventsPublisher eventsPublisher;
    private final Map<String, Executor> childExecutors = new HashMap<>();
    private final Map<String, Consumer<Node>> eventsPublisherByName = new HashMap<>();

    public TreeSuiteExecutor(NodeInvoker nodeInvoker, NodeFallbackManager nodeFallbackManager,
                             Map<String, Executor> childExecutors, NodeExecutionEventsPublisher eventsPublisher) {
        this.nodeInvoker = nodeInvoker;
        this.nodeFallbackManager = nodeFallbackManager;
        this.eventsPublisher = eventsPublisher;
        this.childExecutors.putAll(childExecutors);
        initEventPublisherMap(eventsPublisher);
    }

    private void initEventPublisherMap(NodeExecutionEventsPublisher eventsPublisher) {
        eventsPublisherByName.put(BEFORE_SUB_NODE_NAME + STARTED_EVENT_POSTFIX, eventsPublisher::beforeNodeStarted);
        eventsPublisherByName.put(BEFORE_SUB_NODE_NAME + COMPLETED_EVENT_POSTFIX, eventsPublisher::beforeNodeCompleted);
        eventsPublisherByName.put(BEFORE_SUB_NODE_NAME + ERROR_EVENT_POSTFIX, eventsPublisher::beforeNodeError);
        eventsPublisherByName.put(TARGET_SUB_NODE_NAME + STARTED_EVENT_POSTFIX, eventsPublisher::targetNodeStarted);
        eventsPublisherByName.put(TARGET_SUB_NODE_NAME + COMPLETED_EVENT_POSTFIX, eventsPublisher::targetNodeCompleted);
        eventsPublisherByName.put(TARGET_SUB_NODE_NAME + ERROR_EVENT_POSTFIX, eventsPublisher::targetNodeError);
        eventsPublisherByName.put(AFTER_SUB_NODE_NAME + STARTED_EVENT_POSTFIX, eventsPublisher::afterNodeStarted);
        eventsPublisherByName.put(AFTER_SUB_NODE_NAME + COMPLETED_EVENT_POSTFIX, eventsPublisher::afterNodeCompleted);
        eventsPublisherByName.put(AFTER_SUB_NODE_NAME + ERROR_EVENT_POSTFIX, eventsPublisher::afterNodeError);
    }

    @Override
    public Status executeRoot(Node node) {
        node.setStatus(Status.READY);
        CompletableFuture<Status> rootStatus = CompletableFuture.supplyAsync(() -> executeContainerNode(node), Executors.newSingleThreadExecutor());
        Status status = rootStatus.join();
        node.setStatus(status);
        return status;
    }

    private Status executeContainerNode(Node node) {
        eventsPublisher.containerNodeStarted(node);
        CompletableFuture<Status> resultFuture = CompletableFuture.completedFuture(Status.READY)
                .thenApply(initStatus -> processBeforeSubNodes(node, initStatus))
                .thenCompose(nodeStatus -> processChildSubNodes(node, nodeStatus))
                .thenApply(nodeStatus -> processTargetSubNodes(node, nodeStatus))
                .thenApply(nodeStatus -> processAfterSubNodes(node, nodeStatus));
        Status result = resultFuture.join();
        node.setStatus(result);
        eventsPublisher.containerNodeCompleted(node);
        return result;
    }

    private Status processBeforeSubNodes(Node parentNode, Status initStatus) {
        Status status = processExecutableSubNodes(parentNode.beforeSpliterator(), initStatus, BEFORE_SUB_NODE_NAME);
        parentNode.setStatus(status);
        return status;
    }

    private Status processTargetSubNodes(Node parentNode, Status initStatus) {
        Status status =  processExecutableSubNodes(parentNode.targetSpliterator(), initStatus, TARGET_SUB_NODE_NAME);
        parentNode.setStatus(status);
        return status;
    }

    private Status processAfterSubNodes(Node parentNode, Status initStatus) {
        Status status =  processExecutableSubNodes(parentNode.afterSpliterator(), initStatus, AFTER_SUB_NODE_NAME);
        parentNode.setStatus(status);
        return status;
    }

    private Status processExecutableSubNodes(Spliterator<Node> subNodesSpliterator,
                                                  Status initStatus, String stageName) {
        List<CompletableFuture<Status>> subNodeResults = StreamSupport.stream(subNodesSpliterator, false)
                .filter(subNode -> subNode instanceof ExecutableNode)
                .map(subNode -> CompletableFuture.completedFuture(Status.READY)
                        .thenApply(status -> invokeExecutableNode((ExecutableNode) subNode, stageName))
                        .exceptionally(throwable -> handleStageException(throwable, (ExecutableNode) subNode, stageName)))
                .collect(Collectors.toList());
        CompletableFuture<Void> done = CompletableFuture.allOf(subNodeResults.toArray(new CompletableFuture[0]));
        return done.thenApply(v -> subNodeResults.stream()
                .map(CompletableFuture::join)
                .reduce(initStatus, BinaryOperator.maxBy(Comparator.comparingInt(s -> s.value)))
        ).join();
    }

    private Status invokeExecutableNode(ExecutableNode executableNode, String nodeName) {
        eventsPublisherByName.get(nodeName + STARTED_EVENT_POSTFIX).accept(executableNode);
        Status status;
        if (Status.DRY.equals(executableNode.getStatus())) {
            status = nodeInvoker.dryInvoke(executableNode);
        } else {
            status = nodeInvoker.invoke(executableNode);
        }
        executableNode.setStatus(status);
        eventsPublisherByName.get(nodeName + COMPLETED_EVENT_POSTFIX).accept(executableNode);
        return status;
    }

    private Status handleStageException(Throwable throwable, ExecutableNode stageNode, String stageName) {
        Status errorStatus = nodeFallbackManager.parseThrowable(throwable);
        stageNode.setStatus(errorStatus);
        stageNode.setThrowable(throwable);
        eventsPublisherByName.get(stageName + ERROR_EVENT_POSTFIX).accept(stageNode);
        nodeFallbackManager.conditionallyPrintStacktrace(errorStatus, throwable);
        log.trace(nodeFallbackManager.getErrorMessage(throwable));
        log.error(throwable.getLocalizedMessage());
        return errorStatus;
    }

    private CompletableFuture<Status> processChildSubNodes(Node node, Status nodeStatus) {
        List<CompletableFuture<Status>> childFutures = StreamSupport.stream(node.childSpliterator(), false)
                .filter(containerNode -> containerNode instanceof ContainerNode)
                .map(containerNode -> applyContainerNode((ContainerNode) containerNode))
                .collect(Collectors.toList());
        CompletableFuture<Void> done = CompletableFuture.allOf(childFutures.toArray(new CompletableFuture[0]));
        return done.thenApply(v -> childFutures.stream()
                .map(CompletableFuture::join)
                .reduce(nodeStatus, BinaryOperator.maxBy(Comparator.comparingInt(s -> s.value))));
    }

    private CompletableFuture<Status> applyContainerNode(ContainerNode containerNode) {
        return Optional.ofNullable(childExecutors.get(containerNode.getType() + EXECUTOR_NAME_SUFFIX))
                .map(executor -> CompletableFuture.supplyAsync(() -> executeContainerNode(containerNode), executor))
                .orElseGet(() -> CompletableFuture.completedFuture(executeContainerNode(containerNode)));
    }

    public interface NodeInvoker {

        /**
         * The implementation should care about the node invocation rather about the exception handling.
         * Usually implementation can throw the status COMPLETED.
         * The exception handling will be handled by executor in the next step.
         *
         * @param executableNode - target node for invocation
         * @return - status of invocation, usually COMPLETED.
         */
        Status invoke(ExecutableNode executableNode);

        default Status dryInvoke(ExecutableNode executableNode) {
            return Status.DRY;
        }
    }

}
