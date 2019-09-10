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

package com.griddynamics.qa.sprimber.discovery.support.classic;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.discovery.annotation.TestMapping;
import com.griddynamics.qa.sprimber.discovery.support.TestDefinitionBinder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor
public class ClassicTestBinder implements TestDefinitionBinder {

    private final Method testMethod;

    @Override
    public TestSuiteDefinition.TestDefinition bind() {
        TestMapping testMapping = AnnotationUtils.getAnnotation(testMethod, TestMapping.class);
        String testName = buildTestName(testMethod, testMapping);
        StepDefinition stepDefinition = buildStepDefinition(testMethod, testName);
        val testDefinition = new TestSuiteDefinition.TestDefinition();
        testDefinition.setName(testName);
        testDefinition.setDescription(String.valueOf(AnnotationUtils.getValue(testMapping, "description")));
        testDefinition.setHash(buildTestHash(testMethod));
        testDefinition.getStepDefinitions().add(stepDefinition);
        return testDefinition;
    }

    private StepDefinition buildStepDefinition(Method testMethod, String testName) {
        StepDefinition stepDefinition = new StepDefinition();
        stepDefinition.setMethod(testMethod);
        stepDefinition.setName(testName);
        stepDefinition.setStepPhase(StepDefinition.StepPhase.TEST);
        stepDefinition.setStepType(StepDefinition.StepType.GENERAL);
        stepDefinition.calculateAndSaveHash();
        return stepDefinition;
    }

    private String buildTestName(Method method, TestMapping testMapping) {
        String testName = String.valueOf(AnnotationUtils.getValue(testMapping, "name"));
        return testName.isEmpty() ? method.getName() : testName;
    }

    private String buildTestHash(Method method) {
        String uniqueName = method.getDeclaringClass().getCanonicalName() + "#" +
                method.getName() + "#" +
                method.getParameterCount();
        return DigestUtils.md5DigestAsHex(uniqueName.getBytes());
    }
}
