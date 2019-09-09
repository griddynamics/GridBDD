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

package com.griddynamics.qa.sprimber.reporting.allure;

import io.qameta.allure.SeverityLevel;

import java.util.Arrays;

/**
 * @author fparamonov
 */
public class TagParser {
    private static final String FLAKY = "@FLAKY";
    private static final String KNOWN = "@KNOWN";
    private static final String MUTED = "@MUTED";

    private final com.griddynamics.qa.sprimber.engine.model.TestCase testCase;

    public TagParser(com.griddynamics.qa.sprimber.engine.model.TestCase testCase) {
        this.testCase = testCase;
    }

    protected boolean isFlaky() {
        return getStatusDetailByTag(FLAKY);
    }

    protected boolean isMuted() {
        return getStatusDetailByTag(MUTED);
    }

    protected boolean isKnown() {
        return getStatusDetailByTag(KNOWN);
    }

    protected boolean getStatusDetailByTag(final String tagName) {
        return testCase.getTags().stream()
                .anyMatch(tag -> tag.equalsIgnoreCase(tagName));
    }

    protected boolean isResultTag(String tag) {
        return Arrays.asList(new String[]{FLAKY, KNOWN, MUTED})
                .contains(tag.toUpperCase());
    }

    protected boolean isPureSeverityTag(String tag) {
        return Arrays.stream(SeverityLevel.values())
                .map(SeverityLevel::value)
                .map(value -> "@" + value)
                .anyMatch(value -> value.equalsIgnoreCase(tag));
    }
}
