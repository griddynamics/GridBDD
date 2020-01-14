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

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author fparamonov
 */

@Slf4j
public class StubbedEventPublisher implements NodeExecutionEventsPublisher {

    private ThreadLocal<Integer> depthLevelThreadLocal = new ThreadLocal<>();

    @Override
    public void stageFiltered(Node node) {
        log.info("(sub)stage with role '{}' filtered", node.getRole());
    }

    @Override
    public void stagePrepared(Node node) {
        log.info("(sub)stage with role '{}' prepared", node.getRole());
    }

    @Override
    public void stageStarted(Node node) {
        int currentLevel = Optional.ofNullable(depthLevelThreadLocal.get()).orElse(1);
        currentLevel++;
        log.info("{}(sub)stage with role '{}' started", getIndents(currentLevel), node.getRole());
        currentLevel++;
        depthLevelThreadLocal.set(currentLevel);
    }

    @Override
    public void stageFinished(Node node) {
        int currentLevel = Optional.ofNullable(depthLevelThreadLocal.get()).orElse(1);
        currentLevel--;
        log.info("{}(sub)stage with role '{}' completed with {}", getIndents(currentLevel), node.getRole(), node.getCurrentState());
        currentLevel--;
        depthLevelThreadLocal.set(currentLevel);
    }

    @Override
    public void beforeNodeStarted(Node node) {
        log.info("{}Before Sub Node started {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void beforeNodeCompleted(Node node) {
        log.info("{}Before Sub Node completed {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void beforeNodeError(Node node) {
        log.info("{}Before Sub Node completed exceptionally {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void targetNodeStarted(Node node) {
        log.info("{}Target Sub Node started {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void targetNodeCompleted(Node node) {
        log.info("{}Target Sub Node completed {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void targetNodeError(Node node) {
        log.info("{}Target Sub Node completed exceptionally {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void afterNodeStarted(Node node) {
        log.info("{}After Sub Node started {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void afterNodeCompleted(Node node) {
        log.info("{}After Sub Node completed {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void afterNodeError(Node node) {
        log.info("{}After Sub Node completed exceptionally {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    private String getIndents(int depthLevel) {
        return IntStream.rangeClosed(0, depthLevel).mapToObj(i -> "\t").collect(Collectors.joining());
    }
}
