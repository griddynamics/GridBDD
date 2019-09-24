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

import com.griddynamics.qa.sprimber.discovery.TestSuite;
import com.griddynamics.qa.sprimber.discovery.annotation.TestController;
import com.griddynamics.qa.sprimber.discovery.annotation.TestMapping;
import com.griddynamics.qa.sprimber.discovery.support.TestSuiteDiscovery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicSuiteDiscovery implements TestSuiteDiscovery {

    private final ClassicTestBinder classicTestBinder;
    private final ApplicationContext applicationContext;

    @Override
    public TestSuite discover() {
        TestSuite testSuite = new TestSuite();
        applicationContext.getBeansWithAnnotation(TestController.class).values().stream()
                .map(this::testCaseDiscover)
                .forEach(testCase -> testSuite.getTestCases().add(testCase));
        return testSuite;
    }

    private TestSuite.TestCase testCaseDiscover(Object testController) {
        val testCase = new TestSuite.TestCase();
        List<TestSuite.Test> tests = Arrays.stream(testController.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(TestMapping.class))
                .map(classicTestBinder::bind)
                .peek(test -> test.setParentId(testCase.getRuntimeId()))
                .collect(Collectors.toList());
        testCase.getTests().addAll(tests);
        return testCase;
    }
}
