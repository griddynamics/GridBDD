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

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * @author fparamonov
 */

@Slf4j
@RequiredArgsConstructor
public class TreeSuiteExecutor {

    private final ErrorMapper errorMapper;
    private final NodeFallbackManager nodeFallbackManager;
    private int level = 0;

    public ExecutionResult executeNode(Node node) {
        level++;
        CompletableFuture<ExecutionResult> resultFuture = CompletableFuture.completedFuture(new ExecutionResult(ExecutionResult.Status.PASSED))
                .thenApply(executionResult ->
                        StreamSupport.stream(node.beforeSpliterator(), false)
                                .filter(beforeNode -> beforeNode instanceof ExecutableNode)
                                .map(beforeNode -> processExecutableNode((ExecutableNode) beforeNode))
                                .collect(new ExecutionResult.ExecutionResultCollector())
                )
                .exceptionally(throwable -> handleException(throwable, node))
                .thenApply(executionResult -> StreamSupport.stream(node.childSpliterator(), false)
                        .filter(containerNode -> containerNode instanceof ContainerNode)
                        .peek(nodenode -> log.info("Container Node status: {}", nodenode.getStatus())) // delete
                        .map(this::executeNode)
                        .peek(nodenode -> log.info("Container Node status: {}", nodenode.getStatus())) // delete
                        .collect(new ExecutionResult.ExecutionResultCollector())
                )
                .thenApply(executionResult ->
                        StreamSupport.stream(node.targetSpliterator(), false)
                                .filter(testNode -> testNode instanceof ExecutableNode)
                                .limit(1)
                                .map(testNode -> processExecutableNode((ExecutableNode) testNode))
                                .collect(new ExecutionResult.ExecutionResultCollector())

                )
                .exceptionally(throwable -> handleException(throwable, node))
                .thenApply(executionResult ->
                        StreamSupport.stream(node.afterSpliterator(), false)
                                .filter(afterNode -> afterNode instanceof ExecutableNode)
                                .map(afterNode -> processExecutableNode((ExecutableNode) afterNode))
                                .collect(new ExecutionResult.ExecutionResultCollector())
                )
                .exceptionally(throwable -> handleException(throwable, node))
                .whenComplete(((executionResult, throwable) -> ExecutionResult.builder().executionResult(executionResult).build()));
        ExecutionResult result = resultFuture.join();
//        node.setStatus("COMPLETED");
        log.info("Current Node Result {}", result.getStatus());
        log.info("Current Node Status {}", node.getStatus());
        level--;
        return result;
    }

    private ExecutionResult processExecutableNode(ExecutableNode executableNode) {
        if (Node.Status.SKIP.equals(executableNode.getStatus())) {
            skippedStep();
            return new ExecutionResult(ExecutionResult.Status.SKIPPED);
        }
        ReflectionUtils.invokeMethod(executableNode.getMethod(), this);
        executableNode.setStatus(Node.Status.COMPLETED);
        return new ExecutionResult(ExecutionResult.Status.PASSED);
    }

    private ExecutionResult handleException(Throwable throwable, Node parentNode) {
        ExecutionResult result = errorMapper.parseThrowable(throwable);
        result.conditionallyPrintStacktrace();

        Node.Status errorStatus = nodeFallbackManager.parseThrowable(throwable);
        parentNode.setStatus(errorStatus);
        nodeFallbackManager.conditionallyPrintStacktrace(errorStatus, throwable);
        log.trace(nodeFallbackManager.getErrorMessage(throwable));
        log.error(throwable.getLocalizedMessage());
        return result;
    }

    public void before() {
        sleep(100);
        String tabs = IntStream.rangeClosed(0, level).mapToObj(i -> "\t").collect(Collectors.joining());
        log.info("{}I'm before", tabs);
    }

    public void after() {
        sleep(100);
        String tabs = IntStream.rangeClosed(0, level).mapToObj(i -> "\t").collect(Collectors.joining());
        log.info("{}I'm after", tabs);
    }

    public void step() {
        sleep(100);
        String tabs = IntStream.rangeClosed(0, level).mapToObj(i -> "\t").collect(Collectors.joining());
        log.info("{}I'm step", tabs);
    }

    public void skippedStep() {
        sleep(100);
        String tabs = IntStream.rangeClosed(0, level).mapToObj(i -> "\t").collect(Collectors.joining());
        log.info("{}I'm skipped step", tabs);
    }

    public void exceptionalStep() {
        sleep(100);
        String tabs = IntStream.rangeClosed(0, level).mapToObj(i -> "\t").collect(Collectors.joining());
        log.info("{}I'm exceptional step", tabs);
        throw new RuntimeException("Exception that I want");
    }

    public void sleep(long seconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @RequiredArgsConstructor
    static abstract class Node {

        /**
         * The node can be in one the 3 state - SKIP, ERROR and normal execution.
         * During the SKIP and ERROR state the source spliterators over the sub nodes of current node
         * may produce the next node in normal or in SKIP state.
         * The node in SKIP state allows to control the execution in dry run mode -
         * The all required events will be emitted but the actual code execution will not happens
         * <p>
         * The flags below describes which type of sub node should be seeded skipped status
         * depends on the current node node state - SKIP or ERROR
         * <p>
         * the result of seed flags represented as ORed values from constants below.
         */
        private int subNodesSkippingFlags;
        private Status status;
        private final String runtimeId = UUID.randomUUID().toString();
        private Map<String, List<Node>> children = new HashMap<>();
        private Map<String, Object> attributes = new HashMap<>();

        public Spliterator<Node> childSpliterator() {
            boolean doSeedSkipsOnError = hasFlags(CHILD_ON_ERROR);
            return new NodeSpliterator(children.getOrDefault("child", new ArrayList<>()), false, doSeedSkipsOnError);
        }

        public Spliterator<Node> targetSpliterator() {
            boolean skipTargets = isSkipped() && hasFlags(TARGET_ON_SKIP);
            return new NodeSpliterator(children.getOrDefault("target", new ArrayList<>()), skipTargets);
        }

        public Spliterator<Node> beforeSpliterator() {
            boolean skipBefores = isSkipped() && hasFlags(BEFORE_ON_SKIP);
            return new NodeSpliterator(children.getOrDefault("before", new ArrayList<>()), skipBefores);
        }

        public Spliterator<Node> afterSpliterator() {
            boolean skipAfters = isSkipped() && hasFlags(AFTER_ON_SKIP);
            return new NodeSpliterator(children.getOrDefault("after", new ArrayList<>()), skipAfters);
        }

        private boolean isSkipped() {
            return Status.SKIP.equals(status);
        }

        /**
         * Returns {@code true} if this behaviours flags {@link #subNodesSkippingFlags} contain all of the given flags
         * The default implementation returns true if the corresponding bits
         * of the given characteristics are set.
         *
         * @param flags the flags to check for
         * @return {@code true} if all the specified flags are present,
         * else {@code false}
         */
        private boolean hasFlags(int flags) {
            return (this.subNodesSkippingFlags & flags) == flags;
        }

        /**
         * Seed all before sub nodes in SKIP state when the current node in the SKIP state
         */
        public static final int BEFORE_ON_SKIP = 0x00000001;
        /**
         * Seed all after sub nodes in SKIP state when the current node in the SKIP state
         */
        public static final int AFTER_ON_SKIP = 0x00000002;
        /**
         * Seed all target sub nodes in SKIP state when the current node in the SKIP state
         */
        public static final int TARGET_ON_SKIP = 0x00000004;
        /**
         * Seed all child sub nodes in SKIP state when the current node in the SKIP state
         */
        public static final int CHILD_ON_SKIP = 0x00000008;
        /**
         * Seed all before sub nodes in SKIP state when the current node in the ERROR state
         */
        public static final int BEFORE_ON_ERROR = 0x00000010;
        /**
         * Seed all after sub nodes in SKIP state when the current node in the ERROR state
         */
        public static final int AFTER_ON_ERROR = 0x00000020;
        /**
         * Seed all target sub nodes in SKIP state when the current node in the ERROR state
         */
        public static final int TARGET_ON_ERROR = 0x00000040;
        /**
         * Seed all child sub nodes in SKIP state when the current node in the ERROR state
         */
        public static final int CHILD_ON_ERROR = 0x00000080;

        public static final int READY_STATUS_VALUE = 0;
        public static final int COMPLETED_STATUS_VALUE = 1;
        public static final int SKIP_STATUS_VALUE = 1 << 1;
        public static final int ERROR_STATUS_VALUE = 1 << 2;
        public static final int PENDING_STATUS_VALUE = 1 << 5;
        public static final int FAILED_STATUS_VALUE = 1 << 5;

        /**
         * Used to determine the actual node status. The status present as a "flag word" using int type where:
         * * - first respond to the overall status - possible values READY and COMPLETED
         * * - second respond to the completion status details - possible values SKIP, ERROR
         * * - next respond to the failure details - possible values FAILED
         * <p>
         * it is possible to extend each of this bits with extra information once needed along with new bits
         * with new statuses
         */
        enum Status {
            READY(READY_STATUS_VALUE), COMPLETED(COMPLETED_STATUS_VALUE),
            SKIP(SKIP_STATUS_VALUE | COMPLETED_STATUS_VALUE), ERROR(ERROR_STATUS_VALUE | COMPLETED_STATUS_VALUE),
            PENDING(SKIP_STATUS_VALUE | FAILED_STATUS_VALUE),
            FAILED(ERROR_STATUS_VALUE | FAILED_STATUS_VALUE);

            private static final Map<Integer, Status> BY_CODE = new HashMap<>();

            static {
                for (Status currentStatus : values()) {
                    BY_CODE.put(currentStatus.value, currentStatus);
                }
            }

            public final int value;

            Status(int value) {
                this.value = value;
            }

            public static Status valueOfCode(Integer code) {
                return BY_CODE.get(code);
            }
        }
    }

    static class NodeSpliterator implements Spliterator<Node> {

        boolean skipMode;
        boolean seedSkippedNodesOnError;
        final List<Node> source;
        final Iterator<Node> iterator;

        public NodeSpliterator(List<Node> source, boolean skipMode, boolean seedSkippedNodesOnError) {
            this.source = source;
            this.skipMode = skipMode;
            this.iterator = source.iterator();
            this.seedSkippedNodesOnError = seedSkippedNodesOnError;
        }

        NodeSpliterator(List<Node> source, boolean skipMode) {
            this(source, skipMode, false);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Node> action) {
            if (iterator.hasNext()) {
                applyToNode(action);
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super Node> action) {
            while (iterator.hasNext()) {
                applyToNode(action);
            }
        }

        private void applyToNode(Consumer<? super Node> action) {
            Node node = iterator.next();
            node.setStatus(Node.Status.READY);
            if (skipMode) {
                node.setStatus(Node.Status.SKIP);
            }
            action.accept(node);
            if (Node.Status.ERROR.equals(node.getStatus()) && seedSkippedNodesOnError) {
                skipMode = true;
            }
        }

        @Override
        public Spliterator<Node> trySplit() {
            throw new UnsupportedOperationException("Split not supported for Node spliterator");
        }

        @Override
        public long estimateSize() {
            return source.size();
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    @Data
    static class ContainerNode extends Node {
    }

    @Data
    static class ExecutableNode extends Node {
        private Method method;
        private Throwable throwable;
        private Map<String, Object> parameters = new HashMap<>();
    }

    @RequiredArgsConstructor
    public static class NodeFallbackManager  {

        private final Set<String> skippedExceptionNames;
        private final Set<String> failedExceptionNames;
        private final List<Class<? extends Annotation>> pendingExceptionAnnotations;

        /**
         * Method that allow to translate the error to appropriate status
         *
         * @param throwable the unexpected error during the node execution
         * @return parsed status
         */
        public Node.Status parseThrowable(Throwable throwable) {
            if (throwable.getClass().equals(CompletionException.class)) {
                throwable = throwable.getCause();
            }
            int statusValue = Node.COMPLETED_STATUS_VALUE;
            if (isPendingException(throwable)) {
                statusValue = Node.SKIP_STATUS_VALUE | Node.PENDING_STATUS_VALUE;
            }
            if (skippedExceptionNames.contains(throwable.getClass().getName())) {
                statusValue = Node.SKIP_STATUS_VALUE;
            }
            if (failedExceptionNames.contains(throwable.getClass().getName())) {
                statusValue = Node.ERROR_STATUS_VALUE | Node.FAILED_STATUS_VALUE;
            }
            return Node.Status.valueOfCode(statusValue);
        }

        /**
         * Node can be in state of some failure when fallback strategy activated
         * Available only for node that present in ERROR state
         *
         * @return optional value for throwable that cause this failure.
         */
        public Optional<Throwable> getErrorReason(Throwable throwable) {
            return Optional.ofNullable(throwable);
        }

        public void conditionallyPrintStacktrace(Node.Status status, Throwable throwable) {
            if (status.equals(Node.Status.ERROR) && Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            }
        }

        public String getErrorMessage(Throwable throwable) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            Optional.ofNullable(throwable)
                    .ifPresent(error -> error.printStackTrace(printWriter));
            return stringWriter.getBuffer().toString();
        }

        private boolean isPendingException(Throwable throwable) {
            return pendingExceptionAnnotations.stream()
                    .filter(annotation -> throwable.getClass().isAnnotationPresent(annotation))
                    .count() == 1;
        }
    }
}
