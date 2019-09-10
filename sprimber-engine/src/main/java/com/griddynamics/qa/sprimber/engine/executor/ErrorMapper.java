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

import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status;

/**
 * @author fparamonov
 */
@Component
public class ErrorMapper {

    private final Set<String> skippedExceptionNames;
    private final Set<String> failedExceptionNames;
    private final List<Class<? extends Annotation>> pendingExceptionAnnotations;

    public ErrorMapper(Set<String> skippedExceptionNames,
                       Set<String> failedExceptionNames,
                       List<Class<? extends Annotation>> pendingExceptionAnnotations) {
        this.skippedExceptionNames = skippedExceptionNames;
        this.failedExceptionNames = failedExceptionNames;
        this.pendingExceptionAnnotations = pendingExceptionAnnotations;
    }

    public ExecutionResult parseThrowable(Throwable throwable) {

        if (isPendingException(throwable)) {
            return new ExecutionResult(Status.PENDING, throwable);
        }
        if (skippedExceptionNames.contains(throwable.getClass().getName())) {
            return new ExecutionResult(Status.SKIPPED, throwable);
        }
        if (failedExceptionNames.contains(throwable.getClass().getName())) {
            return new ExecutionResult(Status.FAILED, throwable);
        }
        // TODO: 15/06/2018 to handle here situations where step not found or found more then one
        return new ExecutionResult(Status.BROKEN, throwable);

    }

    private boolean isPendingException(Throwable throwable) {
        return pendingExceptionAnnotations.stream()
                .filter(annotation -> throwable.getClass().isAnnotationPresent(annotation))
                .count() == 1;
    }
}
