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

package com.griddynamics.qa.sprimber.engine.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * @author fparamonov
 */
public class ExecutionResult {

    private final Status status;
    private final Optional<Throwable> optionalError;

    public ExecutionResult(Status status) {
        this.status = status;
        this.optionalError = Optional.empty();
    }

    public ExecutionResult(Status status, Throwable error) {
        this.status = status;
        this.optionalError = Optional.of(error);
    }

    public Status getStatus() {
        return status;
    }

    public Optional<Throwable> getOptionalError() {
        return optionalError;
    }

    /**
     * Execute raw {@code printStackTrace} if there is unknown failures,
     * in other words if the status is BROKEN
     */
    public void conditionallyPrintStacktrace() {
        if (status.equals(Status.BROKEN)) {
            optionalError.ifPresent(Throwable::printStackTrace);
        }
    }

    public String getErrorMessage() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        optionalError.ifPresent(error -> error.printStackTrace(printWriter));
        return stringWriter.getBuffer().toString();
    }

    public enum Status {
        PASSED, SKIPPED, PENDING, FAILED, BROKEN
    }
}
