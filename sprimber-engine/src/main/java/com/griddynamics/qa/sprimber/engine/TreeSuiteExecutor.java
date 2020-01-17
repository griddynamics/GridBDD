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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static com.griddynamics.qa.sprimber.engine.Node.*;

/**
 * @author fparamonov
 */

@Slf4j
class TreeSuiteExecutor implements TreeExecutor {

    public static final String NODE_ASYNC_EXECUTOR_NAME = "node-async-pool";
    private static final String STARTED_EVENT_POSTFIX = "Started";
    private static final String COMPLETED_EVENT_POSTFIX = "Completed";
    private static final String ERROR_EVENT_POSTFIX = "Error";
    private static final String EXECUTOR_NAME_SUFFIX = "Executor";
    private final TreeExecutorContext context;
    private final NodeInvoker nodeInvoker;
    private final NodeExecutionEventsPublisher eventsPublisher;
    private final Map<String, Executor> childExecutors = new HashMap<>();
    private final Map<String, Consumer<Node>> eventsPublisherByName = new HashMap<>();

    public TreeSuiteExecutor(NodeInvoker nodeInvoker,
                             Map<String, Executor> childExecutors,
                             TreeExecutorContext context,
                             NodeExecutionEventsPublisher eventsPublisher) {
        this.nodeInvoker = nodeInvoker;
        this.context = context;
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
    public void executeRoot(Node node) {
        processStage(node);
    }

    private void processStage(Node node) {
        node.scheduleExecution();
        context.startStage(node);
        eventsPublisher.stageStarted(node);
        invokeSubStage(node.beforeSpliterator(context.hasStageException(node)), BEFORE_SUB_NODE_NAME);
        scheduleSubStage(node.childSpliterator(context.hasStageException(node))).join();
        invokeSubStage(node.targetSpliterator(context.hasStageException(node)), TARGET_SUB_NODE_NAME);
        invokeSubStage(node.afterSpliterator(context.hasStageException(node)), AFTER_SUB_NODE_NAME);
        context.waitForFutures(node);
        if (context.hasStageException(node)) {
            node.completeExceptionally(context.getStageException(node));
            context.reportStageException(node);
        } else {
            node.completeSuccessfully();
        }
        eventsPublisher.stageFinished(node);
        context.completeStage(node);
    }

    private void invokeSubStage(Spliterator<Node> subNodesSpliterator, String stageName) {
        StreamSupport.stream(subNodesSpliterator, false)
                .forEach(subNode -> {
                    if (subNode.isAsync()) {
                        CompletableFuture<Void> scheduledNodeInvocation =
                                CompletableFuture.runAsync(() -> invokeNode(stageName, subNode), childExecutors.get(NODE_ASYNC_EXECUTOR_NAME));
                        context.scheduleInvocation(subNode, scheduledNodeInvocation);
                    } else {
                        invokeNode(stageName, subNode);
                    }
                });
    }

    private void invokeNode(String stageName, Node subNode) {
        subNode.prepareExecution();
        eventsPublisherByName.get(stageName + STARTED_EVENT_POSTFIX).accept(subNode);
        boolean skippedByCondition = subNode.getCondition().map(nodeInvoker::shouldSkip).orElse(false);
        if (subNode.isReadyForInvoke() && !skippedByCondition) {
            try {
                nodeInvoker.invoke(subNode);
                subNode.completeSuccessfully();
                eventsPublisherByName.get(stageName + COMPLETED_EVENT_POSTFIX).accept(subNode);
            } catch (Throwable throwable) {
                subNode.completeExceptionally(throwable);
                context.reportStageException(subNode);
                eventsPublisherByName.get(stageName + ERROR_EVENT_POSTFIX).accept(subNode);
            }
        }
        if (subNode.isBypassed() || skippedByCondition) {
            //// TODO: 2020-01-09 add callback for option to execute something extra for BYPASS mode
            subNode.completeWithSkip();
            eventsPublisherByName.get(stageName + COMPLETED_EVENT_POSTFIX).accept(subNode);
        }
    }

    private CompletableFuture<Void> scheduleSubStage(Spliterator<Node> subNodesSpliterator) {
        return CompletableFuture.allOf(StreamSupport.stream(subNodesSpliterator, false)
                .map(this::scheduleSubNode).toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> scheduleSubNode(Node node) {
        return Optional.ofNullable(childExecutors.get(node.getRole() + EXECUTOR_NAME_SUFFIX))
                .map(executor -> CompletableFuture.runAsync(() -> processStage(node), executor))
                .orElseGet(() -> CompletableFuture.allOf().thenRun(() -> processStage(node)));
    }

    /**
     * Sort of a callback interface.
     * Particular implementation will decide how to invoke the actual method
     * depends on applicable fixture framework.
     */
    public interface NodeInvoker {

        boolean shouldSkip(Node.Condition condition);

        /**
         * The implementation should care about the node invocation rather about the exception handling.
         * Usually implementation can throw the status COMPLETED.
         * The exception handling will be handled by executor in the next step.
         *
         * @param executableNode - target node for invocation
         * @return - status of invocation, usually COMPLETED.
         */
        void invoke(Node executableNode);

        default Status dryInvoke(Node executableNode) {
            return Status.SKIP;
        }
    }

}
