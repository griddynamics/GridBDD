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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Slf4j
@RequiredArgsConstructor
public final class SpringStepDefinitionsFactory extends StepDefinitionsAbstractFactory {

    private final ApplicationContext applicationContext;
    private final List<StepDefinitionResolver> resolvers;
    private final List<MarkerAnnotationProvider> markerAnnotationProviders;

    @Override
    List<Method> getMethodCandidates() {
        Map<String, Object> targetBeans = new HashMap<>();
        List<Class<? extends Annotation>> allMarkerAnnotations = markerAnnotationProviders.stream()
                .map(MarkerAnnotationProvider::provide)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        allMarkerAnnotations.forEach(aClass -> targetBeans.putAll(applicationContext.getBeansWithAnnotation(aClass)));
        return targetBeans.values().stream()
                .flatMap(bean -> Arrays.stream(bean.getClass().getDeclaredMethods()))
                .collect(Collectors.toList());
    }

    @Override
    List<StepDefinitionResolver> resolvers() {
        return resolvers;
    }

    interface MarkerAnnotationProvider {

        List<Class<? extends Annotation>> provide();
    }
}
