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

import static com.griddynamics.qa.sprimber.engine.Node.SkipOptions.*;

/**
 * @author fparamonov
 */

public class Node {

    private String name;
    private String description;
    private String historyId;
    private final String role;
    private final String adapterName;
    private final UUID runtimeId = UUID.randomUUID();
    private final Map<String, Object> attributes = new HashMap<>();

    // core related properties
    private Status status;
    private Phase phase;
    private Type type;
    private boolean isDryMode;
    private final boolean isOptional;
    private final Condition condition;

    private Consumer<Node> containerNodeExecutor;
    private Consumer<Node> invokableNodeExecutor;
    private Iterator<SubStage> subStagesIterator;

    private final Node parentNode;
    private final Map<Relation, SubStage> subStages = new EnumMap<>(Relation.class);

    private final Method method;
    private Throwable throwable;
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    private Node(Builder builder, Node parentNode, Type type) {
        this.type = type;
        this.role = builder.role;
        this.adapterName = builder.adapterName;
        this.name = builder.name;
        this.description = builder.description;
        this.historyId = builder.historyId;
        this.condition = builder.condition;
        this.isOptional = builder.isOptional;
        this.method = builder.method;
        this.parameters.putAll(builder.parameters);
        this.attributes.putAll(builder.attributes);
        this.phase = Phase.CREATED;
        this.parentNode = parentNode;
        builder.bypassOptions.forEach((key, value) -> subStages.put(key, new SubStage(key.subStageName(), value)));
    }

    public static Node createRootNode(Builder builder) {
        return new Node(builder, null, Type.HOLDER);
    }

    public UUID getRuntimeId() {
        return this.runtimeId;
    }

    public UUID getParentId() {
        return this.parentNode.runtimeId;
    }

    public Node getParentNodeOfRole(String role) {
        if (parentNode != null) {
            return role.equals(this.getRole()) ? this : parentNode.getParentNodeOfRole(role);
        }
        throw new RuntimeException("Node tree doesn't contains the requested node of role");
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
     *
     * @return adapter name that help to identify the node at runtime
     */
    public String getAdapterName() {
        return this.adapterName;
    }

    public String getCurrentState() {
        return "[ Status: '" + status + "'; Phase: '" + phase + "'; Is dry mode: '" + isDryMode + "'; ]";
    }

    public String getName() {
        return name;
    }

    // TODO: 2020-03-05 remove it and hide into executor
    public Method getMethod() {
        return method;
    }

    // TODO: 2020-03-05 remove it and hide into executor
    public Map<String, Object> getMethodParameters() {
        return new LinkedHashMap<>(parameters);
    }

    public Optional<Throwable> getThrowable() {
        return Optional.ofNullable(throwable);
    }

    public Optional<Condition> getCondition() {
        return Optional.ofNullable(this.condition);
    }

    public boolean isEmptyHolder() {
        return Type.HOLDER.equals(this.type) && this.subStages.values().stream().allMatch(SubStage::isEmpty);
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

    public Node addContainerTarget(Builder builder) {
        return addChildNode(builder, Relation.TARGET, Type.HOLDER);
    }

    public Node addInvokableTarget(Builder builder) {
        return addChildNode(builder, Relation.TARGET, Type.INVOKABLE);
    }

    public Node addInvokableBefore(Builder builder) {
        return addChildNode(builder, Relation.BEFORE, Type.INVOKABLE);
    }

    public Node addInvokableAfter(Builder builder) {
        return addChildNode(builder, Relation.AFTER, Type.INVOKABLE);
    }

    boolean isInvokable() {
        return Type.INVOKABLE.equals(this.type);
    }

    boolean isContainer() {
        return Type.HOLDER.equals(this.type);
    }

    boolean isOptional() {
        return this.isOptional;
    }

    void enableDryMode() {
        this.isDryMode = true;
    }

    boolean isDryMode() {
        return this.isDryMode;
    }

    boolean isRootNode() {
        return parentNode == null;
    }

    void completeExceptionally(Throwable throwable) {
        this.throwable = throwable;
        this.status = Status.ERROR;
        this.phase = Phase.COMPLETED;
    }

    void completeWithSkip() {
        this.status = Status.SKIP;
        this.phase = Phase.COMPLETED;
    }

    void completeSuccessfully() {
        this.status = Status.SUCCESS;
        this.phase = Phase.COMPLETED;
    }

    SubStage nextSubStage() {
        return this.subStagesIterator.next();
    }

    boolean isNextSubStageAvailable() {
        return this.subStagesIterator.hasNext();
    }

    void init() {
        this.subStagesIterator = subStages.values().iterator();
        this.phase = Phase.SCHEDULED;
    }

    void prepareExecution(Consumer<Node> childExecutor, Consumer<Node> targetExecutor) {
        this.containerNodeExecutor = childExecutor;
        this.invokableNodeExecutor = targetExecutor;
        this.status = Status.STARTED;
        this.phase = Phase.EXECUTING;
    }

    void invoke(boolean isSkipped) {
        if (Type.INVOKABLE.equals(this.type)) {
            if (this.isDryMode || isSkipped) {
                completeWithSkip();
            } else {
                invokableNodeExecutor.accept(this);
                // TODO: 2020-03-12 update the completion part for more graceful.
                completeSuccessfully();
            }
        }
        if (Type.HOLDER.equals(this.type)) {
            containerNodeExecutor.accept(this);
        }
    }

    private Node addChildNode(Builder builder, Relation relation, Type type) {
        builder.withAdapterName(adapterName);
        Node node = new Node(builder, this, type);
        this.subStages.get(relation).addChild(node);
        return node;
    }

    static class SubStage {
        private final String name;
        private final List<Node> children = new ArrayList<>();
        private final EnumSet<SkipOptions> skipOptions;

        SubStage(String name,
                 EnumSet<SkipOptions> skipOptions) {
            this.name = name;
            this.skipOptions = skipOptions;
        }

        boolean isEmpty() {
            return children.isEmpty();
        }

        String getName() {
            return name;
        }

        boolean isGlobalStageSkipOnException() {
            return hasSkipOption(BYPASS_WHEN_STAGE_HAS_ERROR);
        }

        boolean isNodeExecutionSkippedOnSubStageException() {
            return hasSkipOption(BYPASS_WHEN_SUB_STAGE_HAS_ERROR);
        }

        Stream<Node> streamSubStageNodes(boolean isDryMode) {
            return children.stream().peek(node -> node.isDryMode = isDryMode);
        }

        void addChild(Node node) {
            children.add(node);
        }

        private boolean hasSkipOption(SkipOptions skipOptions) {
            return this.skipOptions.contains(skipOptions);
        }
    }

    /**
     * Enum that represent possible options when Node or sub stage can be excluded from execution
     */
    public enum SkipOptions {
        BYPASS_WHEN_STAGE_HAS_ERROR, BYPASS_WHEN_SUB_STAGE_HAS_ERROR
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
     * BEFORE - set of nodes that will be scheduled for execution before the TARGET
     * AFTER - set of nodes that will be scheduled for execution after the TARGET
     * TARGET - set of nodes that will be scheduled for execution after BEFORE
     */
    enum Relation {
        BEFORE("before"), TARGET("target"), AFTER("after");

        private String subStageName;

        Relation(String subStageName) {
            this.subStageName = subStageName;
        }

        String subStageName() {
            return this.subStageName;
        }

        static Relation getByName(String name) {
            return Stream.of(Relation.values())
                    .filter(value -> name.equals(value.subStageName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("name not supported"));
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

        private String name;
        private String description;
        private String historyId;
        private String role;
        private String adapterName;
        private boolean isOptional;
        private Condition condition;
        private Method method;
        private final Map<Relation, EnumSet<SkipOptions>> bypassOptions = new EnumMap<>(Relation.class);
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

        public Builder isOptional(boolean isOptional) {
            this.isOptional = isOptional;
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

        public Builder withBeforeBypassOptions(EnumSet<SkipOptions> skipOptions) {
            this.bypassOptions.put(Relation.BEFORE, skipOptions);
            return this;
        }

        public Builder withAfterBypassOptions(EnumSet<SkipOptions> skipOptions) {
            this.bypassOptions.put(Relation.AFTER, skipOptions);
            return this;
        }

        public Builder withTargetBypassOptions(EnumSet<SkipOptions> skipOptions) {
            this.bypassOptions.put(Relation.TARGET, skipOptions);
            return this;
        }
    }
}
