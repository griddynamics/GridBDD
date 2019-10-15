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
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author fparamonov
 */
@Data
@RequiredArgsConstructor
public abstract class Node {

    private String name;
    private String description;
    private String parentId;
    private String historyId;
    // core related properties
    private Status status;
    private final String type;
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
    private final int subNodesSkippingFlags;
    private final String runtimeId = UUID.randomUUID().toString();
    private final Map<String, List<Node>> children = new HashMap<>();
    private final Meta meta = new Meta();
    private final Map<String, Object> attributes = new HashMap<>();

    public void addChild(Node node) {
        node.setParentId(this.runtimeId);
        getChildren().computeIfAbsent(CHILD_SUB_NODE_NAME, k -> new ArrayList<>()).add(node);
    }

    public void addTarget(Node node) {
        node.setParentId(this.runtimeId);
        getChildren().computeIfAbsent(TARGET_SUB_NODE_NAME, k -> new ArrayList<>()).add(node);
    }

    public void addBefore(Node node) {
        node.setParentId(this.runtimeId);
        getChildren().computeIfAbsent(BEFORE_SUB_NODE_NAME, k -> new ArrayList<>()).add(node);
    }

    public void addAfter(Node node) {
        node.setParentId(this.runtimeId);
        getChildren().computeIfAbsent(AFTER_SUB_NODE_NAME, k -> new ArrayList<>()).add(node);
    }

    Spliterator<Node> childSpliterator() {
        boolean switchToDryOnError = hasFlags(SWITCH_TO_DRY_FOR_CHILD);
        boolean dryChildrens = (isDry() && hasFlags(DRY_CHILDES_ON_DRY)) || (isError() && hasFlags(DRY_CHILDES_ON_ERROR));
        return new NodeSpliterator(children.getOrDefault(CHILD_SUB_NODE_NAME, new ArrayList<>()), dryChildrens, switchToDryOnError);
    }

    Spliterator<Node> targetSpliterator() {
        boolean switchToDryOnError = hasFlags(SWITCH_TO_DRY_FOR_TARGET);
        boolean dryTargets = (isDry() && hasFlags(DRY_TARGETS_ON_DRY)) || (isError() && hasFlags(DRY_TARGETS_ON_ERROR));
        return new NodeSpliterator(children.getOrDefault(TARGET_SUB_NODE_NAME, new ArrayList<>()), dryTargets, switchToDryOnError);
    }

    Spliterator<Node> beforeSpliterator() {
        boolean switchToDryOnError = hasFlags(SWITCH_TO_DRY_FOR_BEFORE);
        boolean dryBefores = (isDry() && hasFlags(DRY_BEFORES_ON_DRY)) || (isError() && hasFlags(DRY_BEFORES_ON_ERROR));
        return new NodeSpliterator(children.getOrDefault(BEFORE_SUB_NODE_NAME, new ArrayList<>()), dryBefores, switchToDryOnError);
    }

    Spliterator<Node> afterSpliterator() {
        boolean switchToDryOnError = hasFlags(SWITCH_TO_DRY_FOR_AFTER);
        boolean dryAfters = (isDry() && hasFlags(DRY_AFTERS_ON_DRY)) || (isError() && hasFlags(DRY_AFTERS_ON_ERROR));
        return new NodeSpliterator(children.getOrDefault(AFTER_SUB_NODE_NAME, new ArrayList<>()), dryAfters, switchToDryOnError);
    }

    private boolean isDry() {
        return status.hasStatusFlag(DRY_STATUS_VALUE);
    }

    private boolean isError() {
        return status.hasStatusFlag(ERROR_STATUS_VALUE);
    }

    static final String BEFORE_SUB_NODE_NAME = "before";
    static final String TARGET_SUB_NODE_NAME = "target";
    static final String AFTER_SUB_NODE_NAME = "after";
    static final String CHILD_SUB_NODE_NAME = "child";

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
     * Seed all before sub nodes in DRY state when the current node in the DRY state
     */
    public static final int DRY_BEFORES_ON_DRY = 0x00000001;
    /**
     * Seed all after sub nodes in DRY state when the current node in the DRY state
     */
    public static final int DRY_AFTERS_ON_DRY = 0x00000002;
    /**
     * Seed all target sub nodes in DRY state when the current node in the DRY state
     */
    public static final int DRY_TARGETS_ON_DRY = 0x00000004;
    /**
     * Seed all child sub nodes in DRY state when the current node in the DRY state
     */
    public static final int DRY_CHILDES_ON_DRY = 0x00000008;
    /**
     * Seed all before sub nodes in DRY state when the current node in the ERROR state
     */
    public static final int DRY_BEFORES_ON_ERROR = 0x00000010;
    /**
     * Seed all after sub nodes in DRY state when the current node in the ERROR state
     */
    public static final int DRY_AFTERS_ON_ERROR = 0x00000020;
    /**
     * Seed all target sub nodes in DRY state when the current node in the ERROR state
     */
    public static final int DRY_TARGETS_ON_ERROR = 0x00000040;
    /**
     * Seed all child sub nodes in DRY state when the current node in the ERROR state
     */
    public static final int DRY_CHILDES_ON_ERROR = 0x00000080;
    /**
     * Switch to DRY mode in case of error for BEFORE nodes
     */
    public static final int SWITCH_TO_DRY_FOR_BEFORE = 0x00000100;
    /**
     * Switch to DRY mode in case of error for AFTER nodes
     */
    public static final int SWITCH_TO_DRY_FOR_AFTER = 0x00000200;
    /**
     * Switch to DRY mode in case of error for TARGET nodes
     */
    public static final int SWITCH_TO_DRY_FOR_TARGET = 0x00000400;
    /**
     * Switch to DRY mode in case of error for BEFORE nodes
     */
    public static final int SWITCH_TO_DRY_FOR_CHILD = 0x00000800;

    public static final int READY_STATUS_VALUE = 0;
    public static final int COMPLETED_STATUS_VALUE = 1;
    public static final int DRY_STATUS_VALUE = 1 << 1;
    public static final int SKIP_STATUS_VALUE = 1 << 2;
    public static final int ERROR_STATUS_VALUE = 1 << 3;
    public static final int PENDING_STATUS_VALUE = 1 << 5;
    public static final int FAILED_STATUS_VALUE = 1 << 5;

    /**
     * Used to determine the actual node status. The status present as a "flag word" using int type where:
     * * - first respond to the overall status - possible values READY and COMPLETED
     * * - second respond to the completion status details - possible values DRY, SKIP, ERROR
     * * - next respond to the failure details - possible values FAILED
     * <p>
     * it is possible to extend each of this bits with extra information once needed along with new bits
     * with new statuses
     */
    public enum Status {
        READY(READY_STATUS_VALUE), COMPLETED(COMPLETED_STATUS_VALUE), DRY(DRY_STATUS_VALUE | COMPLETED_STATUS_VALUE),
        SKIP(SKIP_STATUS_VALUE | DRY_STATUS_VALUE | COMPLETED_STATUS_VALUE), ERROR(ERROR_STATUS_VALUE | COMPLETED_STATUS_VALUE),
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

        public boolean hasStatusFlag(int status) {
            return (this.value & status) == status;
        }

    }

    /**
     * The Node Spliterator with feedback.
     * It aimed to walk through the underlain collection of sub nodes and emit one per iteration.
     * The emited node can be in ready(active) or dry state.
     * Feedback allows to switch the spliterator to dry mode depends on the node execution status.
     * Spliterator can also be initialized in DRY mode - emitted nodes will be in DRY status
     * regardless of the execution status.
     */
    static class NodeSpliterator implements Spliterator<Node> {

        /**
         * If true then spliterator will emit nodes in DRY status.
         * Can be changed in runtime depends on Node status after apply
         */
        private boolean dryMode;
        private final boolean switchToDryOnError;
        private final List<Node> source;
        private final Iterator<Node> iterator;

        NodeSpliterator(List<Node> source, boolean dryMode, boolean switchToDryOnError) {
            this.source = source;
            this.dryMode = dryMode;
            this.iterator = source.iterator();
            this.switchToDryOnError = switchToDryOnError;
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
            if (dryMode) {
                node.setStatus(Node.Status.DRY);
            }
            action.accept(node);
            if (node.getStatus().hasStatusFlag(ERROR_STATUS_VALUE) && switchToDryOnError) {
                dryMode = true;
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
    public static class ContainerNode extends Node {
        public ContainerNode(String type) {
            super(type, 0);
        }

        public ContainerNode(String type, int subNodesSkippingFlags) {
            super(type, subNodesSkippingFlags);
        }
    }

    @Data
    public static class ExecutableNode extends Node {
        private Method method;
        private Throwable throwable;
        private Map<String, Object> parameters = new HashMap<>();

        public ExecutableNode(String type) {
            super(type, 0);
        }

        public ExecutableNode(String type, int subNodesSkippingFlags) {
            super(type, subNodesSkippingFlags);
        }
    }

    public static class Meta extends HashMap<String, List<String>> {

        public void addMeta(Meta meta) {
            this.putAll(meta);
        }

        public String getSingleValueOrEmpty(String key) {
            return getSingleValueOrDefault(key, StringUtils.EMPTY);
        }

        public String getSingleValueOrDefault(String key, String defaultValue) {
            return Optional.ofNullable(this.get(key))
                    .map(list -> list.get(0))
                    .orElse(defaultValue);
        }
    }
}
