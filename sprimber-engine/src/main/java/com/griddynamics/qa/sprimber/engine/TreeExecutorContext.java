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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author fparamonov
 */
public class TreeExecutorContext {

    private final Map<UUID, TreeSuiteExecutor.StageStatus> storage = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void startStage(Node node) {
        TreeSuiteExecutor.StageStatus stageStatus = new TreeSuiteExecutor.StageStatus();
        lock.writeLock().lock();
        try {
            storage.put(node.getRuntimeId(), stageStatus);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void completeStage(Node node) {
        lock.writeLock().lock();
        try {
            storage.remove(node.getRuntimeId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void reportStageException(Node node) {
        lock.writeLock().lock();
        try {
            Optional.ofNullable(storage.get(node.getParentId()))
                    .ifPresent(stageStatus -> stageStatus.setHasExceptions(true));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasStageException(Node node) {
        lock.readLock().lock();
        try {
            return storage.get(node.getRuntimeId()).hasExceptions();
        } finally {
            lock.readLock().unlock();
        }
    }
}
