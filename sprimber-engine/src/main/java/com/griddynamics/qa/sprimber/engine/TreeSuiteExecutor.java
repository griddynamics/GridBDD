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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author fparamonov
 */

@Slf4j
class TreeSuiteExecutor implements TreeExecutor {

    private final TreeExecutorContext context;
    private final NodeInvoker nodeInvoker;
    private final Map<String, Executor> childExecutors = new HashMap<>();

    public TreeSuiteExecutor(NodeInvoker nodeInvoker,
                             Map<String, Executor> childExecutors,
                             TreeExecutorContext context) {
        this.nodeInvoker = nodeInvoker;
        this.context = context;
        this.childExecutors.putAll(childExecutors);
    }

    @Override
    public void executeRoot(Node rootNode) {
        context.start(rootNode);
        processStage(rootNode, Node.Relation.TARGET.subStageName());
        context.success(rootNode);
    }

    private void processStage(Node stageHolderNode, String subStageName) {
        // TODO: 2020-03-03 add more graceful handling for empty node
        if (stageHolderNode.isEmptyHolder()) return;

        Executor stageExecutor = Executors.newSingleThreadExecutor();

        stageHolderNode.init();
        while (stageHolderNode.isNextSubStageAvailable()) {
            Node.SubStage subStage = stageHolderNode.nextSubStage();
            CompletableFuture.allOf(subStage.streamSubStageNodes(stageHolderNode.isDryMode())
                    .map(subStageNode -> scheduleSubStageNode(subStageNode, stageExecutor, subStage))
                    .toArray(CompletableFuture[]::new)).join();
        }
        if (context.hasFatalExceptionOnCurrentStage(stageHolderNode)) {
            // TODO: 2020-03-12 handle multiple exceptions for node here
            stageHolderNode.completeExceptionally(context.getStageException(stageHolderNode).get(0));
            context.reportExceptionForParentStage(stageHolderNode, subStageName);
        } else if (stageHolderNode.isDryMode()) {
            stageHolderNode.completeWithSkip();
        } else {
            stageHolderNode.completeSuccessfully();
        }
    }

    private CompletableFuture<Void> scheduleSubStageNode(Node subStageNode, Executor executor, Node.SubStage subStage) {
        subStageNode.prepareExecution(node -> this.processStage(node, subStage.getName()), nodeInvoker::invoke);
        boolean skippedByCondition = subStageNode.getCondition()
                .map(nodeInvoker::isSkippedByCondition)
                .orElse(false);
        return CompletableFuture
                .runAsync(() -> {
                    context.start(subStageNode);
                }, executor)
                .exceptionally(throwable -> {
                    log.error("Exception on start event handling. Message: {}", throwable.getLocalizedMessage());
                    return null;
                })
                .thenApply(v -> {
                    if ((subStage.isGlobalStageSkipOnException() && context.hasFatalExceptionOnParentStage(subStageNode)) ||
                            (subStage.isNodeExecutionSkippedOnSubStageException() && context.hasFatalExceptionOnParentSubStage(subStageNode, subStage.getName()))) {
                        subStageNode.enableDryMode();
                    }
                    subStageNode.invoke(skippedByCondition);
                    return (Consumer<Node>) context::success;
                })
                .exceptionally(throwable -> {
                    subStageNode.completeExceptionally(throwable);
                    return node -> context.error(node, subStage.getName());
                })
                .thenAccept(contextAction -> contextAction.accept(subStageNode))
                .exceptionally(throwable -> {
                    log.error("Exception on complete/error event handling. Message: {}", throwable.getLocalizedMessage());
                    return null;
                });
    }

    /**
     * Sort of a callback interface.
     * Particular implementation will decide how to invoke the actual method
     * depends on applicable fixture framework.
     */
    public interface NodeInvoker {

        boolean isSkippedByCondition(Node.Condition condition);

        /**
         * The implementation should care about the node invocation rather about the exception handling.
         * Usually implementation can throw the status COMPLETED.
         * The exception handling will be handled by executor in the next step.
         *
         * @param executableNode - target node for invocation
         * @return - status of invocation, usually COMPLETED.
         */
        void invoke(Node executableNode);
    }

}
