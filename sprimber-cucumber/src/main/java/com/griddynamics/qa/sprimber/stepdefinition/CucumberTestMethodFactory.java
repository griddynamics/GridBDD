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

package com.griddynamics.qa.sprimber.stepdefinition;

import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fparamonov
 */

class CucumberTestMethodFactory extends TestMethodAbstractFactory {

    private static final String TAGS_ATTRIBUTE = "Tags";
    private static final String TIMEOUT_ATTRIBUTE = "Timeout";
    private static final String ORDER_ATTRIBUTE = "Order";

    private final Map<Class<? extends Annotation>, String> hooks =
            new HashMap<Class<? extends Annotation>, String>() {{
                put(Before.class, "Before Test");
                put(io.cucumber.java.Before.class, "Before Test");
                put(BeforeStep.class, "Before Step");
                put(io.cucumber.java.BeforeStep.class, "Before Step");
                put(After.class, "After Test");
                put(io.cucumber.java.After.class, "After Test");
                put(AfterStep.class, "After Step");
                put(io.cucumber.java.AfterStep.class, "After Step");
            }};

    private final Map<Class<? extends Annotation>, String> steps =
            new HashMap<Class<? extends Annotation>, String>() {{
                put(Given.class, "Given");
                put(io.cucumber.java.en.Given.class, "Given");
                put(When.class, "When");
                put(io.cucumber.java.en.When.class, "When");
                put(Then.class, "Then");
                put(io.cucumber.java.en.Then.class, "Then");
                put(And.class, "And");
                put(io.cucumber.java.en.And.class, "And");
                put(But.class, "But");
                put(io.cucumber.java.en.But.class, "But");
            }};

    public CucumberTestMethodFactory() {
        mapping.putAll(hooks);
        mapping.putAll(steps);
    }

    @Override
    protected TestMethod build(Method method, Annotation annotation) {
        if (hooks.keySet().contains(annotation.annotationType())) {
            return buildHookMethod(method, annotation);
        }
        if (steps.keySet().contains(annotation.annotationType())) {
            return buildStepMethod(method, annotation);
        }
        throw new RuntimeException("Annotation not supported and no methods found");
    }

    private TestMethod buildStepMethod(Method method, Annotation annotation) {
        String name = buildName(method);
        String textPattern = String.valueOf(AnnotationUtils.getValue(annotation));
        TestMethod testMethod = new TestMethod(name, steps.get(annotation.annotationType()), textPattern, method);
        testMethod.addAttribute(TIMEOUT_ATTRIBUTE, AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE.toLowerCase()));
        return testMethod;
    }

    private TestMethod buildHookMethod(Method method, Annotation annotation) {
        String name = buildName(method);
        String textPattern = "Hook step";
        TestMethod testMethod = new TestMethod(name, hooks.get(annotation.annotationType()), textPattern, method);
        testMethod.addAttribute(ORDER_ATTRIBUTE, AnnotationUtils.getValue(annotation, ORDER_ATTRIBUTE.toLowerCase()));
        testMethod.addAttribute(TIMEOUT_ATTRIBUTE, AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE.toLowerCase()));
        if (!Arrays.asList(AnnotationUtils.getValue(annotation)).isEmpty()) {
            testMethod.addAttribute(TAGS_ATTRIBUTE, StringUtils.join((String[]) AnnotationUtils.getValue(annotation), ','));
        }
        return testMethod;
    }

    private String buildName(Method method) {
        return method.getDeclaringClass().getCanonicalName() + "#" + method.getName();
    }
}
