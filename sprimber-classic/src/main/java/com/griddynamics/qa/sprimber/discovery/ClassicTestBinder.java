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

import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.stepdefinition.TestMethod;
import com.griddynamics.qa.sprimber.stepdefinition.TestMethodRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;

import static com.griddynamics.qa.sprimber.engine.Node.*;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ClassicTestBinder {

    private final TestMethodRegistry testMethodRegistry;

    void bind(Node parentNode, Method testCandidate) {
        TestMapping testMapping = AnnotationUtils.getAnnotation(testCandidate, TestMapping.class);
        String testName = buildTestName(testCandidate, testMapping);
        Builder builder = new Builder()
                .withSubNodeModes(BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_CHILDREN_AFTER_ITERATION_ERROR)
                .withRole("test")
                .withName(testName)
                .withDescription(String.valueOf(AnnotationUtils.getValue(testMapping, "description")))
                .withHistoryId(buildTestHistoryId(testCandidate));
        Node test = parentNode.addChild(builder);
        provideStepNode(test, testCandidate);
    }

    private void provideStepNode(Node parentNode, Method method) {
        TestMethod testMethod = testMethodRegistry.streamAllTestMethods()
                .filter(tm -> method.equals(tm.getMethod()))
                .findFirst().orElseThrow(() -> new RuntimeException("No methods found"));
        Builder builder = new Builder()
                .withSubNodeModes(BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_CHILDREN_AFTER_ITERATION_ERROR)
                .withRole("step")
                .withName(method.getName())
                .withMethod(testMethod.getMethod());
        parentNode.addTarget(builder);
    }

    private String buildTestName(Method method, TestMapping testMapping) {
        String testName = String.valueOf(AnnotationUtils.getValue(testMapping, "name"));
        return testName.isEmpty() ? method.getName() : testName;
    }

    private String buildTestHistoryId(Method method) {
        String uniqueName = method.getDeclaringClass().getCanonicalName() + "#" +
                method.getName() + "#" +
                method.getParameterCount();
        return DigestUtils.md5DigestAsHex(uniqueName.getBytes());
    }
}
