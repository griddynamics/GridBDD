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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.griddynamics.qa.sprimber.discovery.ClassicAdapterConstants.*;
import static com.griddynamics.qa.sprimber.engine.Node.Builder;
import static com.griddynamics.qa.sprimber.engine.Node.Bypass.*;

/**
 * @author fparamonov
 */

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ClassicSuiteDiscovery implements TestSuiteDiscovery {

    private static final String NAME_ATTRIBUTE_NAME = "testCaseName";
    private static final String DESCRIPTION_ATTRIBUTE_NAME = "description";
    private final ClassicTestBinder classicTestBinder;
    private final ApplicationContext applicationContext;
    private final TagFilter tagFilter;
    private final Statistic statistic = new Statistic();

    @Override
    public Statistic getDiscoveredInfo() {
        return statistic;
    }

    @Override
    public String name() {
        return "Classic Test Discovery";
    }

    @Override
    public Node discover() {
        Node testSuite = Node.createRootNode(CLASSIC_SUITE_ROLE, CLASSIC_ADAPTER_NAME, EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE,
                BYPASS_AFTER_WHEN_BYPASS_MODE, BYPASS_CHILDREN_AFTER_ITERATION_ERROR));
        applicationContext.getBeansWithAnnotation(TestController.class).values()
                .forEach(testController -> testCaseNodeDiscover(testSuite, testController));
        return testSuite;
    }

    private void testCaseNodeDiscover(Node parentNode, Object testController) {
        TestController controller = testController.getClass().getAnnotation(TestController.class);
        Builder builder = new Builder()
                .withSubNodeModes(EnumSet.of(BYPASS_BEFORE_WHEN_BYPASS_MODE, BYPASS_AFTER_WHEN_BYPASS_MODE,
                        BYPASS_CHILDREN_AFTER_ITERATION_ERROR))
                .withRole(CLASSIC_TEST_CASE_ROLE)
                .withName(String.valueOf(AnnotationUtils.getValue(controller, NAME_ATTRIBUTE_NAME)))
                .withDescription(String.valueOf(AnnotationUtils.getValue(controller, DESCRIPTION_ATTRIBUTE_NAME)));
        Node testCase = parentNode.addChild(builder);

        Arrays.stream(testController.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(TestMapping.class))
                .filter(this::filterTests)
                .forEach(method -> {
                    classicTestBinder.bind(testCase, method);
                    statistic.registerPreparedStage(CLASSIC_TEST_ROLE);
                });
    }

    private boolean filterTests(Method method) {
        TestMapping testMapping = AnnotationUtils.getAnnotation(method, TestMapping.class);
        List<String> tags = Arrays.stream((Object[]) AnnotationUtils.getValue(testMapping, TAGS_ATTRIBUTE))
                .map(o -> (String) o)
                .collect(Collectors.toList());
        boolean isMatched = tagFilter.filter(tags);
        if (!isMatched) {
            statistic.registerFilteredStage(CLASSIC_TEST_ROLE);
        }
        return isMatched;
    }
}
