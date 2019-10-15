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

import com.griddynamics.qa.sprimber.common.TestSuite;
import com.griddynamics.qa.sprimber.engine.Node;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;

import static com.griddynamics.qa.sprimber.engine.Node.*;
import static com.griddynamics.qa.sprimber.engine.Node.DRY_CHILDES_ON_ERROR;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ClassicTestBinder implements TestSuiteDiscovery.TestDefinitionBinder<Method> {

    private final ClassicStepFactory classicStepFactory;

    public Node bindNode(Method testCandidate) {
        Node testNode = new Node.ContainerNode("test",DRY_BEFORES_ON_DRY | DRY_AFTERS_ON_DRY | SWITCH_TO_DRY_FOR_CHILD);
        TestMapping testMapping = AnnotationUtils.getAnnotation(testCandidate, TestMapping.class);
        String testName = buildTestName(testCandidate, testMapping);
        testNode.setName(testName);
        testNode.setHistoryId(buildTestHistoryId(testCandidate));
        testNode.setDescription(String.valueOf(AnnotationUtils.getValue(testMapping, "description")));
        testNode.addChild(classicStepFactory.provideStepNode(testCandidate));
        return testNode;
    }

    @Override
    public TestSuite.Test bind(Method testCandidate) {
        TestMapping testMapping = AnnotationUtils.getAnnotation(testCandidate, TestMapping.class);
        String testName = buildTestName(testCandidate, testMapping);
        val test = new TestSuite.Test();
        test.setName(testName);
        test.setHistoryId(buildTestHistoryId(testCandidate));
        test.setDescription(String.valueOf(AnnotationUtils.getValue(testMapping, "description")));
        test.getSteps().add(classicStepFactory.provideStep(testCandidate));
        test.getSteps().forEach(step -> step.setParentId(test.getRuntimeId()));
        return test;
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
