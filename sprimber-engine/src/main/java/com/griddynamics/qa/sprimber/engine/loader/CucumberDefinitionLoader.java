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

package com.griddynamics.qa.sprimber.engine.loader;

import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.model.action.ActionScope;
import com.griddynamics.qa.sprimber.engine.model.action.ActionType;
import com.griddynamics.qa.sprimber.engine.model.action.Actions;
import com.griddynamics.qa.sprimber.engine.model.action.details.CucumberHookDetails;
import com.griddynamics.qa.sprimber.engine.model.action.details.CucumberStepDetails;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.context.ApplicationContext;
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
public class CucumberDefinitionLoader implements ActionDefinitionLoader {

    private static final String TIMEOUT_ATTRIBUTE = "timeout";
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String ORDER_ATTRIBUTE = "order";

    private final Map<Class<? extends Annotation>, ActionType> typesByActionAnnotation =
            new HashMap<Class<? extends Annotation>, ActionType>() {{
                put(Before.class, ActionType.Before);
                put(After.class, ActionType.After);
                put(BeforeStep.class, ActionType.Before);
                put(AfterStep.class, ActionType.After);
                put(Given.class, ActionType.Given);
                put(When.class, ActionType.When);
                put(Then.class, ActionType.Then);
            }};

    private final Map<Class<? extends Annotation>, ActionScope> scopesByActionAnnotation =
            new HashMap<Class<? extends Annotation>, ActionScope>() {{
                put(Before.class, ActionScope.SCENARIO);
                put(After.class, ActionScope.SCENARIO);
                put(BeforeStep.class, ActionScope.STEP);
                put(AfterStep.class, ActionScope.STEP);
                put(Given.class, ActionScope.STEP);
                put(When.class, ActionScope.STEP);
                put(Then.class, ActionScope.STEP);
            }};

    private final List<Class<? extends Annotation>> hookAnnotations =
            Arrays.asList(Before.class, After.class, BeforeStep.class, AfterStep.class);
    private final List<Class<? extends Annotation>> stepAnnotations =
            Arrays.asList(Given.class, When.class, Then.class);

    private final ApplicationContext applicationContext;

    public CucumberDefinitionLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<ActionDefinition> load() {
        return applicationContext.getBeansWithAnnotation(Actions.class).values().stream()
                .flatMap(actionBean ->
                        Arrays.stream(actionBean.getClass().getDeclaredMethods())
                                .map(this::processMethod))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<ActionDefinition> processMethod(Method method) {
        return typesByActionAnnotation.keySet().stream()
                .map(annotationClass -> AnnotationUtils.findAnnotation(method, annotationClass))
                .filter(Objects::nonNull)
                .map(annotation -> buildDefinitionFromAnnotation(annotation, method)).findFirst();
    }

    private ActionDefinition buildDefinitionFromAnnotation(Annotation annotation, Method method) {
        ActionDefinition definition = new ActionDefinition();
        definition.setMethod(method);
        definition.setActionType(typesByActionAnnotation.get(annotation.annotationType()));
        definition.setActionScope(scopesByActionAnnotation.get(annotation.annotationType()));
        if (hookAnnotations.contains(annotation.annotationType())) {
            resolveHookAnnotation(annotation, definition);
        }
        if (stepAnnotations.contains(annotation.annotationType())) {
            resolveStepAnnotation(annotation, definition);
        }
        return definition;
    }

    private void resolveHookAnnotation(Annotation annotation, ActionDefinition actionDefinition) {
        // TODO: 06/06/2018 add helper text to test field of action definition
        CucumberHookDetails cucumberHookDetails = new CucumberHookDetails();
        cucumberHookDetails.setOrder((Integer) AnnotationUtils.getValue(annotation, ORDER_ATTRIBUTE));
        cucumberHookDetails.setTimeout((Long) AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE));
        cucumberHookDetails.getValues().addAll(Arrays.asList((String[]) AnnotationUtils.getValue(annotation, VALUE_ATTRIBUTE)));
        actionDefinition.setMetaInfo(cucumberHookDetails);
    }

    private void resolveStepAnnotation(Annotation annotation, ActionDefinition actionDefinition) {
        actionDefinition.setActionText((String) AnnotationUtils.getValue(annotation, VALUE_ATTRIBUTE));
        CucumberStepDetails cucumberStepDetails = new CucumberStepDetails();
        cucumberStepDetails.setTimeout((Long) AnnotationUtils.getValue(annotation, TIMEOUT_ATTRIBUTE));
        actionDefinition.setMetaInfo(cucumberStepDetails);
    }
}
