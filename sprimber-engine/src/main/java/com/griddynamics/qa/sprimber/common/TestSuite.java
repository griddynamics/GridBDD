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

package com.griddynamics.qa.sprimber.common;

import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author fparamonov
 */

@Data
public class TestSuite {

    private ExecutionResult executionResult;
    private List<TestCase> testCases = new ArrayList<>();

    @Data
    public static class TestCase {

        private final String runtimeId = UUID.randomUUID().toString();
        private String name;
        private String description;
        private ExecutionResult executionResult;
        private List<Test> tests = new ArrayList<>();
    }

    @Data
    public static class Test {

        private final String runtimeId = UUID.randomUUID().toString();
        private boolean isFallbackActive;
        private String name;
        private String description;
        private String parentId;
        private String historyId;
        private Meta meta = new Meta();
        private ExecutionResult executionResult;
        private FallbackStrategy fallbackStrategy;
        private List<Step> steps = new ArrayList<>();
        private Map<String, Object> attributes = new HashMap<>();

        public void activeFallback() {
            this.isFallbackActive = true;
        }

        public static class Meta extends HashMap<String, List<String>> {

            public String getSingleValueOrEmpty(String key) {
                return getSingleValueOrDefault(key, StringUtils.EMPTY);
            }

            public String getSingleValueOrDefault(String key, String defaultValue) {
                return Optional.ofNullable(this.get(key))
                        .map(list -> list.get(0))
                        .orElse(defaultValue);
            }
        }
    }

    @Data
    public static class Step {

        private final String runtimeId = UUID.randomUUID().toString();
        private boolean isSkipped;
        private String name;
        private String parentId;
        private String resolvedTextPattern;
        private StepDefinition stepDefinition;
        private ExecutionResult executionResult;
        private Map<String, Object> parameters = new HashMap<>();

        public boolean isHookStep() {
            return this.stepDefinition.getStepType().equals(StepDefinition.StepType.BEFORE) || this.stepDefinition.getStepType().equals(StepDefinition.StepType.AFTER);
        }

        public boolean isGeneralStep() {
            return this.stepDefinition.getStepType().equals(StepDefinition.StepType.GENERAL);
        }
    }

    public interface FallbackStrategy {

        List<StepDefinition.StepType> allowedTypes();

        List<StepDefinition.StepPhase> allowedPhases();

        void updateScope(Step executedStep);
    }
}
