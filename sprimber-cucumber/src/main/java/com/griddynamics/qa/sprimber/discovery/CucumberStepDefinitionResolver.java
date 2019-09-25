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

import com.griddynamics.qa.sprimber.common.StepDefinition;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fparamonov
 */

class CucumberStepDefinitionResolver extends StepDefinitionsAbstractResolver {

    public static final String TAGS_ATTRIBUTE = "Tags";
    private static final String TIMEOUT_ATTRIBUTE = "Timeout";
    private static final String ORDER_ATTRIBUTE = "Order";

    private final Map<Class<? extends Annotation>, Pair<StepDefinition.StepType, StepDefinition.StepPhase>> hooks =
            new HashMap<Class<? extends Annotation>, Pair<StepDefinition.StepType, StepDefinition.StepPhase>>() {{
                put(Before.class, new ImmutablePair<>(StepDefinition.StepType.BEFORE, StepDefinition.StepPhase.TEST));
                put(BeforeStep.class, new ImmutablePair<>(StepDefinition.StepType.BEFORE, StepDefinition.StepPhase.STEP));
                put(After.class, new ImmutablePair<>(StepDefinition.StepType.AFTER, StepDefinition.StepPhase.TEST));
                put(AfterStep.class, new ImmutablePair<>(StepDefinition.StepType.AFTER, StepDefinition.StepPhase.STEP));
            }};

    private final Map<Class<? extends Annotation>, Pair<StepDefinition.StepType, StepDefinition.StepPhase>> steps =
            new HashMap<Class<? extends Annotation>, Pair<StepDefinition.StepType, StepDefinition.StepPhase>>() {{
                put(Given.class, new ImmutablePair<>(StepDefinition.StepType.GIVEN, StepDefinition.StepPhase.STEP));
                put(When.class, new ImmutablePair<>(StepDefinition.StepType.WHEN, StepDefinition.StepPhase.STEP));
                put(Then.class, new ImmutablePair<>(StepDefinition.StepType.THEN, StepDefinition.StepPhase.STEP));
                put(And.class, new ImmutablePair<>(StepDefinition.StepType.GENERAL, StepDefinition.StepPhase.STEP));
                put(But.class, new ImmutablePair<>(StepDefinition.StepType.GENERAL, StepDefinition.StepPhase.STEP));
            }};

    public CucumberStepDefinitionResolver() {
        mapping.putAll(hooks);
        mapping.putAll(steps);
    }

    @Override
    protected StepDefinition applyCustomTransformation(StepDefinition stepDefinition, Method method, Annotation annotation) {
        if (hooks.keySet().contains(annotation.annotationType())) {
            processHookAttributes(annotation, stepDefinition);
        }
        if (steps.keySet().contains(annotation.annotationType())) {
            processStepAttributes(annotation, stepDefinition);
        }
        return stepDefinition;
    }

    private void processStepAttributes(Annotation annotation, StepDefinition definition) {
        definition.getAttributes().put(TIMEOUT_ATTRIBUTE, AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE.toLowerCase()));
        definition.setBindingTextPattern(String.valueOf(AnnotationUtils.getValue(annotation)));
    }

    private void processHookAttributes(Annotation annotation, StepDefinition definition) {
        definition.getAttributes().put(ORDER_ATTRIBUTE, AnnotationUtils.getValue(annotation, ORDER_ATTRIBUTE.toLowerCase()));
        definition.getAttributes().put(TIMEOUT_ATTRIBUTE, AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE.toLowerCase()));
        if (!Arrays.asList(AnnotationUtils.getValue(annotation)).isEmpty()) {
            definition.getAttributes().put(TAGS_ATTRIBUTE, StringUtils.join((String[]) AnnotationUtils.getValue(annotation), ','));
        }
    }
}
