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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author fparamonov
 */

public class ExecutionResult {

    private final Status status;
    private final Throwable throwable;
    private final Map<Status, Long> statistic = new HashMap<>();

    public ExecutionResult(Status status) {
        this.status = status;
        this.throwable = null;
    }

    public ExecutionResult(Status status, Throwable error) {
        this.status = status;
        this.throwable = error;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<Throwable> getOptionalError() {
        return Optional.ofNullable(throwable);
    }

    public Map<Status, Long> getStatistic() {
        return statistic;
    }

    /**
     * Execute raw {@code printStackTrace} if there is unknown failures,
     * in other words if the status is BROKEN
     */
    public void conditionallyPrintStacktrace() {
        if (status.equals(Status.BROKEN) && Objects.nonNull(throwable)) {
            throwable.printStackTrace();
        }
    }

    public String getErrorMessage() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Optional.ofNullable(throwable)
                .ifPresent(error -> error.printStackTrace(printWriter));
        return stringWriter.getBuffer().toString();
    }

    public static ExecutionResultBuilder builder() {
        return new ExecutionResultBuilder();
    }

    public static class ExecutionResultBuilder {

        private Status status = Status.PASSED;
        private Throwable throwable;
        private Map<Status, Long> statistic = new HashMap<>();

        private ExecutionResultBuilder() {
        }

        public ExecutionResultBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public ExecutionResultBuilder executionResult(ExecutionResult executionResult) {
            if (!(this.status.equals(Status.FAILED) || this.status.equals(Status.BROKEN))) {
                this.status = executionResult.getStatus();
            }
            this.throwable = executionResult.getOptionalError().orElse(null);
            this.statistic.compute(executionResult.getStatus(), (k, v) -> Objects.isNull(v) ? 1L : v++);
            return this;
        }

        public ExecutionResult build() {
            ExecutionResult executionResult = new ExecutionResult(this.status, this.throwable);
            executionResult.getStatistic().putAll(this.statistic);
            return executionResult;
        }
    }

    public static class ExecutionResultCollector implements Collector<ExecutionResult, ExecutionResultBuilder, ExecutionResult> {

        @Override
        public Supplier<ExecutionResultBuilder> supplier() {
            return ExecutionResult::builder;
        }

        @Override
        public BiConsumer<ExecutionResultBuilder, ExecutionResult> accumulator() {
            return ExecutionResult.ExecutionResultBuilder::executionResult;
        }

        @Override
        public BinaryOperator<ExecutionResultBuilder> combiner() {
            return (e1, e2) -> null;
        }

        @Override
        public Function<ExecutionResultBuilder, ExecutionResult> finisher() {
            return ExecutionResult.ExecutionResultBuilder::build;
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

    public enum Status {
        PASSED, SKIPPED, PENDING, FAILED, BROKEN
    }
}
