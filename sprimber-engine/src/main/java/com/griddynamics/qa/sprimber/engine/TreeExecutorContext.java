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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * @author fparamonov
 */
public class TreeExecutorContext {

    private final Map<UUID, StageDetails> storage = new ConcurrentHashMap<>();
    private final Map<UUID, FutureContainer> futures = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> scheduledStageToGrantParent = new ConcurrentHashMap<>();
    private final ReadWriteLock storageLock = new ReentrantReadWriteLock();
    private final ReadWriteLock futureLock = new ReentrantReadWriteLock();

    void startStage(Node node) {
        StageDetails stageDetails = new StageDetails();
        storageLock.writeLock().lock();
        try {
            storage.put(node.getRuntimeId(), stageDetails);
        } finally {
            storageLock.writeLock().unlock();
        }
    }

    void waitForFutures(Node node) {
        Optional.ofNullable(getFutureContainer(node)).ifPresent(c -> {
            if (c.getJoinStage().equals(node.getRole())) {
                CompletableFuture
                        .allOf(futures.get(node.getRuntimeId()).streamFutures().toArray(CompletableFuture[]::new))
                        .join();
            } else {
                rescheduleFutureContainer(node, c);
                scheduledStageToGrantParent.merge(node.getRuntimeId(), node.getParentId(), (childId, oldParentId) -> node.getParentId());
            }
        });
    }

    private void rescheduleFutureContainer(Node node, FutureContainer c) {
        futureLock.writeLock().lock();
        try {
            futures.putIfAbsent(node.getParentId(), new FutureContainer(c.getJoinStage()));
            futures.get(node.getParentId()).merge(c);
            futures.remove(node.getRuntimeId());
        } finally {
            futureLock.writeLock().unlock();
        }
    }

    private FutureContainer getFutureContainer(Node node) {
        FutureContainer container;
        futureLock.readLock().lock();
        try {
            container = futures.get(node.getRuntimeId());
        } finally {
            futureLock.readLock().unlock();
        }
        return container;
    }

    void scheduleInvocation(Node node, CompletableFuture future) {
        futureLock.writeLock().lock();
        try {
            futures.putIfAbsent(node.getParentId(), new FutureContainer(node.joinStage()));
            futures.get(node.getParentId()).addFuture(future);
        } finally {
            futureLock.writeLock().unlock();
        }
    }

    void completeStage(Node node) {
        storageLock.writeLock().lock();
        try {
            storage.remove(node.getRuntimeId());
        } finally {
            storageLock.writeLock().unlock();
        }
    }

    void reportStageException(Node node) {
        storageLock.writeLock().lock();
        try {
            Optional.ofNullable(storage.get(node.getParentId()))
                    .ifPresent(sd -> sd.registerException(node.getThrowable().get()));
            Optional.ofNullable(scheduledStageToGrantParent.get(node.getParentId()))
                    .map(storage::get)
                    .ifPresent(sd -> sd.registerException(node.getThrowable().get()));
        } finally {
            storageLock.writeLock().unlock();
        }
    }

    boolean hasStageException(Node node) {
        storageLock.readLock().lock();
        try {
            return storage.get(node.getRuntimeId()).hasExceptions();
        } finally {
            storageLock.readLock().unlock();
        }
    }

    // TODO: 2020-01-16 add support for multiply exceptions at node level
    Throwable getStageException(Node node) {
        storageLock.readLock().lock();
        try {
            return storage.get(node.getRuntimeId()).getFirstException();
        } finally {
            storageLock.readLock().unlock();
        }
    }

    static class StageDetails {
        private boolean hasExceptions;
        private final List<Throwable> exceptionList = new ArrayList<>();

        boolean hasExceptions() {
            return hasExceptions;
        }

        void registerException(Throwable throwable) {
            this.hasExceptions = true;
            exceptionList.add(throwable);
        }

        Throwable getFirstException() {
            return exceptionList.get(0);
        }
    }

    static class FutureContainer {
        private final List<CompletableFuture> futureList = new ArrayList<>();
        private final String joinStage;

        FutureContainer(String joinStage) {
            this.joinStage = joinStage;
        }

        void addFuture(CompletableFuture future) {
            futureList.add(future);
        }

        void merge(FutureContainer container) {
            container.streamFutures().forEach(futureList::add);
        }

        Stream<CompletableFuture> streamFutures() {
            return futureList.stream();
        }

        String getJoinStage() {
            return joinStage;
        }
    }
}
