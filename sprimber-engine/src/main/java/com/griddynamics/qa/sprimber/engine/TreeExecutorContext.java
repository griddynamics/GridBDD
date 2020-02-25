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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */
public class TreeExecutorContext {

    private final Map<UUID, StageDetails> storage = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final NodeExecutionEventsPublisher eventsPublisher;

    public TreeExecutorContext(NodeExecutionEventsPublisher eventsPublisher) {
        this.eventsPublisher = eventsPublisher;
    }

    void start(Node node) {
        if (node.isContainer()) {
            initStage(node);
            eventsPublisher.stageStarted(node);
        }
        if (node.isInvokable()) {
            eventsPublisher.nodeStarted(node);
        }
    }

    void success(Node node) {
        if (node.isContainer()) {
            eventsPublisher.stageFinished(node);
        }
        if (node.isInvokable()) {
            eventsPublisher.nodeFinished(node);
        }
    }

    void error(Node node, String subStageName) {
        reportExceptionForParentStage(node, subStageName);
        if (node.isContainer()) {
            eventsPublisher.stageError(node);
        }
        if (node.isInvokable()) {
            eventsPublisher.nodeError(node);
        }
    }

    private void initStage(Node node) {
        StageDetails stageDetails = new StageDetails();
        lock.writeLock().lock();
        try {
            storage.put(node.getRuntimeId(), stageDetails);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void completeStage(Node node) {
        lock.writeLock().lock();
        try {
            storage.remove(node.getRuntimeId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addObjectForStage(Node currentNode, String stageName, String objectName, Object value) {
        lock.readLock().lock();
        try {
            storage.get(currentNode.getParentNodeOfRole(stageName).getRuntimeId()).registerStageObject(objectName, value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Object getObjectForStage(Node currentNode, String stageName, String objectName) {
        lock.readLock().lock();
        try {
            return storage.get(currentNode.getParentNodeOfRole(stageName).getRuntimeId()).getStageObject(objectName);
        } finally {
            lock.readLock().unlock();
        }
    }

    void reportExceptionForParentStage(Node node, String subStageName) {
        lock.writeLock().lock();
        try {
            if (node.isRootNode()) return;
            if (node.isOptional()) {
                Optional.ofNullable(storage.get(node.getParentId()))
                        .ifPresent(stageDetails -> stageDetails.registerSoftException(Node.Relation.getByName(subStageName), node.getThrowable().get()));
            } else {
                Optional.ofNullable(storage.get(node.getParentId()))
                        .ifPresent(stageDetails -> stageDetails.registerFatalException(Node.Relation.getByName(subStageName), node.getThrowable().get()));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    void reportSoftExceptionForParentStage(Node node, String subStageName) {
        lock.writeLock().lock();
        try {
            if (node.isRootNode()) return;
            Optional.ofNullable(storage.get(node.getParentId()))
                    .ifPresent(stageDetails -> stageDetails.registerSoftException(Node.Relation.getByName(subStageName), node.getThrowable().get()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    boolean hasFatalExceptionOnParentStage(Node node) {
        lock.readLock().lock();
        try {
            return storage.get(node.getParentId()).hasFatalExceptions();
        } finally {
            lock.readLock().unlock();
        }
    }

    boolean hasFatalExceptionOnCurrentStage(Node node) {
        lock.readLock().lock();
        try {
            return storage.get(node.getRuntimeId()).hasFatalExceptions();
        } finally {
            lock.readLock().unlock();
        }
    }

    boolean hasFatalExceptionOnParentSubStage(Node node, String subStageName) {
        lock.readLock().lock();
        try {
            return storage.get(node.getParentId()).hasFatalExceptions(Node.Relation.getByName(subStageName));
        } finally {
            lock.readLock().unlock();
        }
    }

    // TODO: 2020-01-16 add support for multiply exceptions at node level
    List<Throwable> getStageException(Node node) {
        lock.readLock().lock();
        try {
            return storage.get(node.getRuntimeId()).getAllExceptions();
        } finally {
            lock.readLock().unlock();
        }
    }

    static class StageDetails {
        private Map<Node.Relation, List<Throwable>> fatalExceptionsBySubStage = new EnumMap<>(Node.Relation.class);
        private Map<Node.Relation, List<Throwable>> softExceptionsBySubStage = new EnumMap<>(Node.Relation.class);
        private Map<String, Object> stageObjects = new HashMap<>();

        void registerStageObject(String objectName, Object value) {
            stageObjects.put(objectName, value);
        }

        Object getStageObject(String name) {
            return stageObjects.get(name);
        }

        boolean hasFatalExceptions() {
            return fatalExceptionsBySubStage.values().stream().mapToLong(Collection::size).sum() > 0;
        }

        boolean hasFatalExceptions(Node.Relation relation) {
            return Optional.ofNullable(fatalExceptionsBySubStage.get(relation))
                    .map(list -> !list.isEmpty())
                    .orElse(false);
        }

        void registerFatalException(Node.Relation relation, Throwable throwable) {
            fatalExceptionsBySubStage.putIfAbsent(relation, new ArrayList<>());
            fatalExceptionsBySubStage.get(relation).add(throwable);
        }

        void registerSoftException(Node.Relation relation, Throwable throwable) {
            softExceptionsBySubStage.putIfAbsent(relation, new ArrayList<>());
            softExceptionsBySubStage.get(relation).add(throwable);
        }

        List<Throwable> getAllExceptions() {
            List<Throwable> exceptions = new ArrayList<>();
            exceptions.addAll(fatalExceptionsBySubStage.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
            exceptions.addAll(softExceptionsBySubStage.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
            return exceptions;
        }
    }
}
