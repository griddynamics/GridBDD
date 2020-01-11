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

import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;

/**
 * @author fparamonov
 */
@RequiredArgsConstructor
public class NodeFallbackManager {

    private final Set<String> skippedExceptionNames;
    private final Set<String> failedExceptionNames;
    private final List<Class<? extends Annotation>> pendingExceptionAnnotations;

    /**
     * Method that allow to translate the error to appropriate status
     *
     * @param throwable the unexpected error during the node execution
     * @return parsed status
     */
    public Node.Status parseThrowable(Throwable throwable) {
        if (throwable.getClass().equals(CompletionException.class)) {
            throwable = throwable.getCause();
        }
//        int statusValue = Node.ERROR_STATUS_VALUE | Node.COMPLETED_STATUS_VALUE;
//        if (isPendingException(throwable)) {
//            statusValue = Node.SKIP_STATUS_VALUE | Node.PENDING_STATUS_VALUE;
//        }
//        if (skippedExceptionNames.contains(throwable.getClass().getName())) {
//            statusValue = Node.SKIP_STATUS_VALUE;
//        }
//        if (failedExceptionNames.contains(throwable.getClass().getName())) {
//            statusValue = Node.ERROR_STATUS_VALUE | Node.FAILED_STATUS_VALUE;
//        }
//        return Node.Status.valueOfCode(statusValue);
        return Node.Status.ERROR;
    }

    /**
     * Node can be in state of some failure when fallback strategy activated
     * Available only for node that present in ERROR state
     *
     * @return optional value for throwable that cause this failure.
     */
    public Optional<Throwable> getErrorReason(Throwable throwable) {
        return Optional.ofNullable(throwable);
    }

    public void conditionallyPrintStacktrace(Node.Status status, Throwable throwable) {
//        if (!status.hasStatusFlag(Node.FAILED_STATUS_VALUE) && Objects.nonNull(throwable)) {
//            if (throwable.getClass().equals(CompletionException.class)) {
//                throwable.getCause().printStackTrace();
//            } else {
//                throwable.printStackTrace();
//            }
//        }
    }

    public String getErrorMessage(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Optional.ofNullable(throwable)
                .ifPresent(error -> error.printStackTrace(printWriter));
        return stringWriter.getBuffer().toString();
    }

    private boolean isPendingException(Throwable throwable) {
        return pendingExceptionAnnotations.stream()
                .filter(annotation -> throwable.getClass().isAnnotationPresent(annotation))
                .count() == 1;
    }
}
