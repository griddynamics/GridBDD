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
import org.springframework.util.ReflectionUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author fparamonov
 */

@Slf4j
public class StubbedNodeInvoker implements TreeSuiteExecutor.NodeInvoker {

    @Override
    public Node.Status invoke(Node.ExecutableNode executableNode) {
        ReflectionUtils.invokeMethod(executableNode.getMethod(), this);
        return Node.Status.COMPLETED;
    }

    @Override
    public Node.Status dryInvoke(Node.ExecutableNode executableNode) {
        skippedStep();
        return Node.Status.SKIP;
    }

    public void before() {
        sleep(100);
        log.info("I'm before");
    }

    public void after() {
        sleep(100);
        log.info("I'm after");
    }

    public void step() {
        sleep(100);
        log.info("I'm step");
    }

    public void skippedStep() {
        sleep(100);
        log.info("I'm skipped step");
    }

    public void exceptionalStep() {
        sleep(100);
        log.info("I'm exceptional step");
        throw new RuntimeException("Exception that I want");
    }

    public void sleep(long seconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
