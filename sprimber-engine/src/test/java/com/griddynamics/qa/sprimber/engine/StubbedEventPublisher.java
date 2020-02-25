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

    private ThreadLocal<Integer> depthLevelThreadLocal = ThreadLocal.withInitial(() -> 0);

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
    public void stageError(Node parentNode) {
        log.info("{} Stage Node completed exceptionally {}", getIndents(depthLevelThreadLocal.get()), parentNode.getCurrentState());
    }

    @Override
    public void nodeStarted(Node node) {
        log.info("{} Sub Node started {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void nodeFinished(Node node) {
        log.info("{} Sub Node completed {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    @Override
    public void nodeError(Node node) {
        log.info("{} Sub Node completed exceptionally {}", getIndents(depthLevelThreadLocal.get()), node.getCurrentState());
    }

    private String getIndents(int depthLevel) {
        return IntStream.rangeClosed(0, depthLevel).mapToObj(i -> "\t").collect(Collectors.joining());
    }
}
