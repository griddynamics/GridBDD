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

package com.griddynamics.qa.sprimber.engine.executor;

import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.model.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.griddynamics.qa.sprimber.engine.model.ThreadConstants.SPRIMBER_TS_EXECUTOR_NAME;

/**
 * @author fparamonov
 */

@Component
public class TestSuiteExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteExecutor.class);

    private final TestCaseExecutor testCaseExecutor;

    public TestSuiteExecutor(TestCaseExecutor testCaseExecutor) {
        this.testCaseExecutor = testCaseExecutor;
    }

    @Async(SPRIMBER_TS_EXECUTOR_NAME)
    public CompletableFuture<ExecutionResult> execute(TestSuite testSuite) {

        CompletableFuture.allOf(testSuite.getTestCases().stream()
                .map(testCaseExecutor::execute).toArray(CompletableFuture[]::new)).join();

        // TODO: 2019-06-08 Add actual status for test suite execution
        ExecutionResult successResult = new ExecutionResult(ExecutionResult.Status.PASSED);
        return CompletableFuture.completedFuture(successResult);
    }
}
