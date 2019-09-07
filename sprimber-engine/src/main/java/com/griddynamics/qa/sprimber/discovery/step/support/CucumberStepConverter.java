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

package com.griddynamics.qa.sprimber.discovery.step.support;

import com.griddynamics.qa.sprimber.discovery.step.StepDefinition;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Component
public class CucumberStepConverter implements StepDefinitionConverter {

    private static final String TIMEOUT_ATTRIBUTE = "Timeout";
    private static final String TAGS_ATTRIBUTE = "Tags";
    private static final String VALUE_ATTRIBUTE = "value";
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
    private final Map<Class<? extends Annotation>, Pair<StepDefinition.StepType, StepDefinition.StepPhase>> hooksAndSteps =
            new HashMap<>();

    public CucumberStepConverter() {
        hooksAndSteps.putAll(hooks);
        hooksAndSteps.putAll(steps);
    }

    @Override
    public List<StepDefinition> convert(Method method) {
        return hooksAndSteps.keySet().stream()
                .map(annotationClass -> AnnotationUtils.getAnnotation(method, annotationClass))
                .filter(Objects::nonNull)
                .map(annotation -> buildDefinitionFromAnnotation(method, annotation))
                .collect(Collectors.toList());
    }

    private StepDefinition buildDefinitionFromAnnotation(Method method, Annotation annotation) {
        StepDefinition definition = new StepDefinition();
        definition.setMethod(method);
        definition.setStepType(hooksAndSteps.get(annotation.annotationType()).getLeft());
        definition.setStepPhase(hooksAndSteps.get(annotation.annotationType()).getRight());
        definition.setName(method.getName());
        if (hooks.keySet().contains(annotation.annotationType())) {
            processHookProperties(annotation, definition);
        }
        if (steps.keySet().contains(annotation.annotationType())) {
            processStepProperties(annotation, definition);
        }
        return definition;
    }

    private void processStepProperties(Annotation annotation, StepDefinition definition) {
        definition.getParameters().put(TIMEOUT_ATTRIBUTE, AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE.toLowerCase()));
        definition.setBindingTextPattern(String.valueOf(AnnotationUtils.getValue(annotation, VALUE_ATTRIBUTE)));
    }

    private void processHookProperties(Annotation annotation, StepDefinition definition) {
        definition.getParameters().put(ORDER_ATTRIBUTE, AnnotationUtils.getValue(annotation, ORDER_ATTRIBUTE.toLowerCase()));
        definition.getParameters().put(TIMEOUT_ATTRIBUTE, AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE.toLowerCase()));
        if (!Arrays.asList(AnnotationUtils.getValue(annotation)).isEmpty()) {
            definition.getParameters().put(TAGS_ATTRIBUTE, StringUtils.join((String[]) AnnotationUtils.getValue(annotation, VALUE_ATTRIBUTE), ','));
        }
    }
}
