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

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.griddynamics.qa.sprimber.engine.Node.Bypass.*;

/**
 * @author fparamonov
 */

public class Node {

    private String name;
    private String description;
    private String historyId;
    private final String role;
    private final Map<String, Object> attributes = new HashMap<>();

    // core related properties
    private Status status;
    private Phase phase;
    private Type type;
    private final String adapterName;
    private boolean isBypassed;
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
    private final EnumSet<Bypass> subNodeExecutionModes;
    private final Condition condition;
    private final UUID parentId;
    private final UUID runtimeId = UUID.randomUUID();
    private final Map<Relation, List<Node>> children = new EnumMap<>(Relation.class);

    private final Method method;
    private Throwable throwable;
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    private Node(UUID parentId, Type type, String role, String adapterName, Method method, EnumSet<Bypass> subNodeExecutionModes) {
        this.type = type;
        this.role = role;
        this.adapterName = adapterName;
        this.parentId = parentId;
        this.subNodeExecutionModes = subNodeExecutionModes;
        this.method = method;
        this.phase = Phase.CREATED;
        this.condition = null;
    }

    private Node(Builder builder, UUID parentId, Type type) {
        this.type = type;
        this.parentId = parentId;
        this.role = builder.role;
        this.adapterName = builder.adapterName;
        this.name = builder.name;
        this.description = builder.description;
        this.historyId = builder.historyId;
        this.subNodeExecutionModes = builder.subNodeExecutionModes;
        this.condition = builder.condition;
        this.method = builder.method;
        this.parameters.putAll(builder.parameters);
        this.attributes.putAll(builder.attributes);
    }

    public static Node createRootNode(String role, String adapterName, EnumSet<Bypass> subNodesSkippingFlags) {
        return new Node(UUID.randomUUID(), Type.HOLDER, role, adapterName, null, subNodesSkippingFlags);
    }

    public UUID getRuntimeId() {
        return this.runtimeId;
    }

    public UUID getParentId() {
        return this.parentId;
    }

    public String getHistoryId() {
        return this.historyId;
    }

    public String getDescription() {
        return this.description;
    }

    public Optional<Object> getAttribute(String name) {
        return Optional.ofNullable(this.attributes.get(name));
    }

    public Stream<Map.Entry<String, Object>> attributesStream() {
        return this.attributes.entrySet().stream();
    }

    public String getRole() {
        return this.role;
    }

    /**
     * Return unique name for adapter that was responsible for Node creation and lifecycle maintenance
     * in scope of custom functionality like reporting
     * @return adapter name that help to identify the node at runtime
     */
    public String getAdapterName() {
        return adapterName;
    }

    public String getCurrentState() {
        return "[ Status: '" + status + "'; Phase: '" + phase + "'; Is bypassed: '" + isBypassed + "'; ]";
    }

    public String getName() {
        return name;
    }

    public Method getMethod() {
        return method;
    }

    public Map<String, Object> getMethodParameters() {
        return new LinkedHashMap<>(parameters);
    }

    public Optional<Throwable> getThrowable() {
        return Optional.ofNullable(throwable);
    }

    public Optional<Condition> getCondition() {
        return Optional.ofNullable(this.condition);
    }

    public boolean isReadyForInvoke() {
        return Type.INVOKABLE.equals(type) && !isBypassed && Status.STARTED.equals(status) && Phase.EXECUTING.equals(phase);
    }

    public void completePreparation() {
        this.phase = Phase.PREPARED;
    }

    public void scheduleExecution() {
        this.phase = Phase.SCHEDULED;
    }

    public void bypassExecution() {
        this.isBypassed = true;
    }

    public boolean isBypassed() {
        return this.isBypassed;
    }

    public boolean isEmptyHolder() {
        return Type.HOLDER.equals(this.type) && this.children.isEmpty();
    }

    public void prepareExecution() {
        this.status = Status.STARTED;
        this.phase = Phase.EXECUTING;
    }

    public void completeExceptionally(Throwable throwable) {
        this.throwable = throwable;
        this.status = Status.ERROR;
        this.phase = Phase.COMPLETED;
    }

    public void completeWithSkip() {
        this.status = Status.SKIP;
        this.phase = Phase.COMPLETED;
    }

    public void completeSuccessfully() {
        this.status = Status.SUCCESS;
        this.phase = Phase.COMPLETED;
    }

    public boolean isCompletedExceptionally() {
        return Phase.COMPLETED.equals(phase) && Status.ERROR.equals(status);
    }

    public boolean isCompletedSuccessfully() {
        return Phase.COMPLETED.equals(phase) && Status.SUCCESS.equals(status);
    }

    public boolean isCompletedWithSkip() {
        return Phase.COMPLETED.equals(phase) && Status.SKIP.equals(status);
    }

    public Node addChild(Builder builder) {
        builder.withAdapterName(this.adapterName);
        Node node = new Node(builder, this.runtimeId, Type.HOLDER);
        this.children.computeIfAbsent(Relation.CHILD, k -> new ArrayList<>()).add(node);
        return node;
    }

    public Node addChild(String role, EnumSet<Bypass> subNodesSkippingFlags) {
        Node node = new Node(this.runtimeId, Type.HOLDER, role, this.adapterName, null, subNodesSkippingFlags);
        this.children.computeIfAbsent(Relation.CHILD, k -> new ArrayList<>()).add(node);
        return node;
    }

    public Node addTarget(Builder builder) {
        builder.withAdapterName(this.adapterName);
        Node node = new Node(builder, this.runtimeId, Type.INVOKABLE);
        this.children.computeIfAbsent(Relation.TARGET, k -> new ArrayList<>()).add(node);
        return node;
    }

    public Node addTarget(String role, Method method) {
        Node node = new Node(this.runtimeId, Type.INVOKABLE, role, this.adapterName, method, EnumSet.noneOf(Bypass.class));
        this.children.computeIfAbsent(Relation.TARGET, k -> new ArrayList<>()).add(node);
        return node;
    }

    public Node addBefore(Builder builder) {
        builder.withAdapterName(this.adapterName);
        Node node = new Node(builder, this.runtimeId, Type.INVOKABLE);
        this.children.computeIfAbsent(Relation.BEFORE, k -> new ArrayList<>()).add(node);
        return node;
    }

    public Node addBefore(String role, Method method) {
        Node node = new Node(this.runtimeId, Type.INVOKABLE, role, this.adapterName, method, EnumSet.noneOf(Bypass.class));
        this.children.computeIfAbsent(Relation.BEFORE, k -> new ArrayList<>()).add(node);
        return node;
    }

    public Node addAfter(Builder builder) {
        builder.withAdapterName(this.adapterName);
        Node node = new Node(builder, this.runtimeId, Type.INVOKABLE);
        this.children.computeIfAbsent(Relation.AFTER, k -> new ArrayList<>()).add(node);
        return node;
    }

    public Node addAfter(String role, Method method) {
        Node node = new Node(this.runtimeId, Type.INVOKABLE, role, this.adapterName, method, EnumSet.noneOf(Bypass.class));
        this.children.computeIfAbsent(Relation.AFTER, k -> new ArrayList<>()).add(node);
        return node;
    }

    Spliterator<Node> childSpliterator(boolean hasErrorOnPreviousStage) {
        boolean bypassNextNodesAfterError = hasFlag(BYPASS_CHILDREN_AFTER_ITERATION_ERROR);
        boolean bypassMode = (isBypassed() && hasFlag(BYPASS_CHILDREN_WHEN_BYPASS_MODE)) || (hasErrorOnPreviousStage && hasFlag(BYPASS_CHILDREN_AFTER_STAGE_ERROR));
        return new NodeSpliterator(children.getOrDefault(Relation.CHILD, new ArrayList<>()), bypassMode, bypassNextNodesAfterError);
    }

    Spliterator<Node> targetSpliterator(boolean hasErrorOnPreviousStage) {
        boolean bypassNextNodesAfterError = hasFlag(BYPASS_TARGET_AFTER_ITERATION_ERROR);
        boolean bypassMode = (isBypassed() && hasFlag(BYPASS_TARGET_WHEN_BYPASS_MODE)) || (hasErrorOnPreviousStage && hasFlag(BYPASS_TARGET_AFTER_STAGE_ERROR));
        return new NodeSpliterator(children.getOrDefault(Relation.TARGET, new ArrayList<>()), bypassMode, bypassNextNodesAfterError);
    }

    Spliterator<Node> beforeSpliterator(boolean hasErrorOnPreviousStage) {
        boolean bypassNextNodesAfterError = hasFlag(BYPASS_BEFORE_AFTER_ITERATION_ERROR);
        boolean bypassMode = (isBypassed() && hasFlag(BYPASS_BEFORE_WHEN_BYPASS_MODE)) || (hasErrorOnPreviousStage && hasFlag(BYPASS_BEFORE_AFTER_STAGE_ERROR));
        return new NodeSpliterator(children.getOrDefault(Relation.BEFORE, new ArrayList<>()), bypassMode, bypassNextNodesAfterError);
    }

    Spliterator<Node> afterSpliterator(boolean hasErrorOnPreviousStage) {
        boolean bypassNextNodesAfterError = hasFlag(BYPASS_AFTER_AFTER_ITERATION_ERROR);
        boolean bypassMode = (isBypassed() && hasFlag(BYPASS_AFTER_WHEN_BYPASS_MODE)) || (hasErrorOnPreviousStage && hasFlag(BYPASS_AFTER_AFTER_STAGE_ERROR));
        return new NodeSpliterator(children.getOrDefault(Relation.AFTER, new ArrayList<>()), bypassMode, bypassNextNodesAfterError);
    }

    static final String BEFORE_SUB_NODE_NAME = "before";
    static final String TARGET_SUB_NODE_NAME = "target";
    static final String AFTER_SUB_NODE_NAME = "after";
    static final String CHILD_SUB_NODE_NAME = "child";

    /**
     * Returns {@code true} if this behaviours flags {@link #subNodeExecutionModes} contain all of the given flags
     * The default implementation returns true if the corresponding bits
     * of the given characteristics are set.
     *
     * @param flag the flag to check for
     * @return {@code true} if all the specified flags are present,
     * else {@code false}
     */
    private boolean hasFlag(Bypass flag) {
        return this.subNodeExecutionModes.contains(flag);
    }

    /**
     * Next bypass modes are available:
     * * BYPASS_BEFORE_WHEN_BYPASS_MODE - Seed all before sub nodes in BYPASS mode when the current node in the BYPASS mode
     * * BYPASS_AFTER_WHEN_BYPASS_MODE - Seed all after sub nodes in BYPASS mode when the current node in the BYPASS mode
     * * BYPASS_TARGET_WHEN_BYPASS_MODE - Seed all target sub nodes in BYPASS mode when the current node in the BYPASS mode
     * * BYPASS_CHILDREN_WHEN_BYPASS_MODE - Seed all children sub nodes in BYPASS mode when the current node in the BYPASS mode
     * * BYPASS_BEFORE_AFTER_STAGE_ERROR - Seed all before sub nodes in BYPASS mode when the current node in the ERROR state
     * * BYPASS_AFTER_AFTER_STAGE_ERROR - Seed all after sub nodes in BYPASS mode when the current node in the ERROR state
     * * BYPASS_TARGET_AFTER_STAGE_ERROR - Seed all target sub nodes in BYPASS mode when the current stage(node) in the ERROR state
     * * BYPASS_CHILDREN_AFTER_STAGE_ERROR - Seed all child sub nodes in BYPASS mode when the current node in the ERROR state
     * * BYPASS_BEFORE_AFTER_ITERATION_ERROR - Switch iterator to seed nodes in BYPASS mode after error with previous node execution
     * * BYPASS_AFTER_AFTER_ITERATION_ERROR - Switch iterator to seed nodes in BYPASS mode after error with previous node execution
     * * BYPASS_TARGET_AFTER_ITERATION_ERROR - Switch iterator to seed nodes in BYPASS mode after error with previous node execution
     * * BYPASS_CHILDREN_AFTER_ITERATION_ERROR - Switch iterator to seed nodes in BYPASS mode after error with previous node execution
     * *
     */
    public enum Bypass {
        BYPASS_BEFORE_WHEN_BYPASS_MODE,
        BYPASS_AFTER_WHEN_BYPASS_MODE,
        BYPASS_TARGET_WHEN_BYPASS_MODE,
        BYPASS_CHILDREN_WHEN_BYPASS_MODE,
        BYPASS_BEFORE_AFTER_STAGE_ERROR,
        BYPASS_AFTER_AFTER_STAGE_ERROR,
        BYPASS_TARGET_AFTER_STAGE_ERROR,
        BYPASS_CHILDREN_AFTER_STAGE_ERROR,
        BYPASS_BEFORE_AFTER_ITERATION_ERROR,
        BYPASS_AFTER_AFTER_ITERATION_ERROR,
        BYPASS_TARGET_AFTER_ITERATION_ERROR,
        BYPASS_CHILDREN_AFTER_ITERATION_ERROR
    }

    /**
     * Node can be on one of the following statuses after the processing by executor:
     * * STARTED - initial executing status
     * * SKIP - the node was executed in BYPASS mode, so no actual code invocation
     * * SUCCESS - node executed completed without any errors(no Throwable)
     * * ERROR - during node execution error occurred
     */
    enum Status {
        STARTED, SKIP, SUCCESS, ERROR
    }

    /**
     * Node will follow next lifecycle:
     * * CREATED - initial state of node, once the object initialised
     * * PREPARED - once the discovery completed the node is ready for executing
     * and all necessary fields are initialized
     * * SCHEDULED - if the node marked for differed execution once the node picked up by executor
     * and passed to the executor
     * * EXECUTING - means that node currently in executing state
     * * COMPLETED - execution if over, for details check the {@link Status}
     */
    enum Phase {
        CREATED, PREPARED, SCHEDULED, EXECUTING, COMPLETED
    }

    /**
     * Node can play the next functions:
     * HOLDER - just a holder node that handle their child, cannot be executed
     * INVOKABLE - leaf node, cannot have children as holders for next nodes, but may be invokable
     */
    enum Type {
        HOLDER, INVOKABLE
    }

    /**
     * Node may have one of the next relations to the children
     * BEFORE - must be a INVOKABLE node, will be executed before the proceeding to the next
     * AFTER - must be a INVOKABLE node, will be executed after all previous children executions
     * TARGET - must be a INVOKABLE node, the final logical end of the tree branch
     * CHILD - HOLDER node that contains information about the tree extension
     */
    enum Relation {
        BEFORE, CHILD, TARGET, AFTER
    }

    /**
     * The Node Spliterator with feedback.
     * It aimed to walk through the underlain collection of sub nodes and emit one per iteration.
     * The emitted node can be in ready(active) or bypass mode.
     * Feedback allows to switch the spliterator to bypass mode depends on the node execution status.
     * Spliterator can also be initialized in BYPASS mode - emitted nodes will be in BYPASS mode
     */
    static class NodeSpliterator implements Spliterator<Node> {

        /**
         * If true then spliterator will emit nodes in BYPASS mode.
         * BYPASS means that executor will process nodes in regular order but will never execute them
         * Can be changed in runtime depends on Node status after apply
         */
        private boolean bypassMode;
        private final boolean bypassNextNodesAfterError;
        private final List<Node> source;
        private final Iterator<Node> iterator;

        NodeSpliterator(List<Node> source, boolean bypassMode, boolean bypassNextNodesAfterError) {
            this.source = source;
            this.bypassMode = bypassMode;
            this.iterator = source.iterator();
            this.bypassNextNodesAfterError = bypassNextNodesAfterError;
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
            node.scheduleExecution();
            if (bypassMode) {
                node.bypassExecution();
            }
            action.accept(node);
            if (node.isCompletedExceptionally() && bypassNextNodesAfterError) {
                bypassMode = true;
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

    public interface Condition {

        Object getTriggerType();

        boolean match(Object actualValue);
    }

    public static final class Builder {

        private EnumSet<Bypass> subNodeExecutionModes;
        private String name;
        private String description;
        private String historyId;
        private String role;
        private String adapterName;
        private Condition condition;
        private Method method;
        private final Map<String, Object> attributes = new HashMap<>();
        private final Map<String, Object> parameters = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withHistoryId(String historyId) {
            this.historyId = historyId;
            return this;
        }

        public Builder withRole(String role) {
            this.role = role;
            return this;
        }

        public Builder withAdapterName(String adapterName) {
            this.adapterName = adapterName;
            return this;
        }

        public Builder withSubNodeModes(EnumSet<Bypass> subNodeExecutionModes) {
            this.subNodeExecutionModes = subNodeExecutionModes;
            return this;
        }

        public Builder withCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Builder withAttribute(String attributeName, Object attributeValue) {
            this.attributes.put(attributeName, attributeValue);
            return this;
        }

        public Builder withParameters(Map<String, Object> methodParameters) {
            this.parameters.putAll(methodParameters);
            return this;
        }

        public Builder withMethod(Method method) {
            this.method = method;
            return this;
        }
    }
}
