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

import com.fasterxml.jackson.databind.ObjectMapper;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import io.cucumber.stepexpression.StepExpressionFactory;
import io.cucumber.stepexpression.TypeRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Locale;

/**
 * @author fparamonov
 */

@Configuration
public class SprimberCucumberConfiguration {

    @Configuration
    @Import({CucumberClassMarkerProvider.class, CucumberStepDefinitionResolver.class, CucumberSuiteDiscovery.class,
            CucumberTestBinder.class, PickleStepFactory.class, TagFilter.class
    })
    static class DiscoveryConfiguration {
    }

    @Configuration
    static class ThirdPartyConfiguration {
        @Bean
        public Parser<GherkinDocument> gherkinParser() {
            return new Parser<>(new AstBuilder());
        }

        @Bean
        public TokenMatcher tokenMatcher() {
            return new TokenMatcher();
        }

        @Bean
        public Compiler compiler() {
            return new Compiler();
        }

        @Bean
        @ConditionalOnMissingBean
        public ObjectMapper typeRegistryObjectMapper() {
            return new ObjectMapper();
        }

        @Bean
        @ConditionalOnMissingBean
        public StepExpressionFactory stepExpressionFactory() {
            return new StepExpressionFactory(new TypeRegistry(Locale.ENGLISH));
        }

        @Bean
        @ConditionalOnMissingBean
        public TypeRegistry typeRegistry(ObjectMapper typeRegistryObjectMapper) {
            TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
            JacksonDataTableTransformer jacksonDataTableTransformer = new JacksonDataTableTransformer(typeRegistryObjectMapper);
            typeRegistry.dataTableTypeRegistry().setDefaultDataTableEntryTransformer(jacksonDataTableTransformer);
            typeRegistry.dataTableTypeRegistry().setDefaultDataTableCellTransformer(jacksonDataTableTransformer);
            return typeRegistry;
        }
    }
}
