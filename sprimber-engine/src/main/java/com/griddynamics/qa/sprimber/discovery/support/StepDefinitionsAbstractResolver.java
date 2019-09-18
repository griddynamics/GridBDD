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

package com.griddynamics.qa.sprimber.discovery.support;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

public abstract class StepDefinitionsAbstractResolver implements StepDefinitionsFactory.StepDefinitionResolver {

    protected Map<Class<? extends Annotation>, Pair<StepDefinition.StepType, StepDefinition.StepPhase>> mapping = new HashMap<>();

    @Override
    public boolean accept(Annotation annotation) {
        return mapping.keySet().contains(annotation.annotationType());
    }

    @Override
    public boolean accept(Method method) {
        return Arrays.stream(method.getDeclaredAnnotations()).anyMatch(this::accept);
    }

    @Override
    public List<StepDefinition> resolve(Method method) {
        return mapping.keySet().stream()
                .map(annotationClass -> AnnotationUtils.getAnnotation(method, annotationClass))
                .filter(Objects::nonNull)
                .map(annotation -> initStepDefinitionWithCustomTransformation(method, annotation))
                .collect(Collectors.toList());
    }

    private StepDefinition initStepDefinitionWithCustomTransformation(Method method, Annotation annotation) {
        StepDefinition definition = new StepDefinition();
        definition.setMethod(method);
        definition.setStepType(mapping.get(annotation.annotationType()).getLeft());
        definition.setStepPhase(mapping.get(annotation.annotationType()).getRight());
        definition.setName(method.getDeclaringClass().getCanonicalName() + "#" + method.getName());
        return applyCustomTransformation(definition, method, annotation);

    }

    protected abstract StepDefinition applyCustomTransformation(StepDefinition stepDefinition, Method method, Annotation annotation);
}
