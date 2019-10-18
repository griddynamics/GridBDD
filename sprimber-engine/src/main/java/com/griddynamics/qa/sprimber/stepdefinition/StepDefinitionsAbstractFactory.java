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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */
abstract class StepDefinitionsAbstractFactory implements StepDefinitionsFactory {

    /**
     * Method will apply all available step definition resolvers into all applicable methods
     * and build the aggregate map of step definitions
     *
     * @return the collection of the step definitions keyed by step definition hash
     */
    public Map<String, StepDefinition> getStepDefinitions() {
        return getMethodCandidates().stream()
                .flatMap(method -> resolvers().stream()
                        .filter(resolver -> resolver.accept(method))
                        .map(resolver -> resolver.resolve(method))
                        .flatMap(Collection::stream))
                .collect(Collectors.toMap(StepDefinition::getHash, v -> v));
    }

    /**
     * The implementation should make the search and find the all methods that can mapped to the step definitions
     *
     * @return collection of method candidates
     */
    abstract List<Method> getMethodCandidates();

    /**
     * The implementation should provide all available step definition resolvers
     *
     * @return the collection that contains step definition resolvers
     */
    abstract List<StepDefinitionResolver> resolvers();
}
