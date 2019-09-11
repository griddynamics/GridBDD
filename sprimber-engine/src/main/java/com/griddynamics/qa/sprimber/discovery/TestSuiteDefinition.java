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

package com.griddynamics.qa.sprimber.discovery;

import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import lombok.Data;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author fparamonov
 */

@Data
public class TestSuiteDefinition {

    private TestExecutor testExecutor;
    private ExecutionResult executionResult;
    private List<TestCaseDefinition> testCaseDefinitions = new ArrayList<>();

    @Data
    public static class TestCaseDefinition {

        private ExecutionResult executionResult;
        private List<TestDefinition> testDefinitions = new ArrayList<>();
    }

    @Data
    public static class TestDefinition {

        private String hash;
        private String name;
        private String description;
        private String runtimeId = UUID.randomUUID().toString();
        private ExecutionResult executionResult;
        private List<StepDefinition> stepDefinitions = new ArrayList<>();
        private Map<String, Object> attributes = new HashMap<>();
    }

    /**
     * Main difference happens in exact test execution, since the suite and testcase execution pretty straightforward
     * for most cases(plain code, bdd). To provide this custom logic for each test execution implement this interface
     * Each {@link TestSuiteDefinition} should be aware about how to execute the tests under
     */
    public interface TestExecutor {
        CompletableFuture<ExecutionResult> execute(TestDefinition testDefinition);
    }
}
