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

package com.griddynamics.qa.sprimber.discovery.support.cucumber;

import com.griddynamics.qa.sprimber.discovery.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.discovery.support.TestSuiteDiscovery;
import com.griddynamics.qa.sprimber.engine.model.configuration.SprimberProperties;
import com.griddynamics.qa.sprimber.engine.processor.cucumber.CucumberDocument;
import com.griddynamics.qa.sprimber.engine.processor.cucumber.PickleStepProcessor;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author fparamonov
 */

@Component
@RequiredArgsConstructor
public class CucumberFeaturesDiscovery implements TestSuiteDiscovery {

    private final SprimberProperties sprimberProperties;
    private final Parser<GherkinDocument> gherkinParser;
    private final TokenMatcher tokenMatcher;
    private final Compiler compiler;
    private final PickleStepProcessor pickleStepProcessor;
    private final ApplicationContext applicationContext;

    @Override
    public TestSuiteDefinition discover() {
        TestSuiteDefinition testSuiteDefinition = new TestSuiteDefinition();
        try {
             Arrays.stream(applicationContext.getResources(sprimberProperties.getFeaturePath()))
                    .map(this::buildCucumberDocument)
                    .map(this::testCaseDiscover)
                     .forEach(testCaseDefinition -> testSuiteDefinition.getTestCaseDefinitions().add(testCaseDefinition));
        } catch (IOException e) {
            // TODO: 2019-09-10 handle the exception from resource unavailability correctly
            e.printStackTrace();
        }
        return testSuiteDefinition;
    }

    private TestSuiteDefinition.TestCaseDefinition testCaseDiscover(CucumberDocument cucumberDocument) {
        val testCaseDefinition = new TestSuiteDefinition.TestCaseDefinition();
        compiler.compile(cucumberDocument.getDocument())
                .forEach(pickle -> {
                    CucumberTestBinder cucumberTestBinder = new CucumberTestBinder(pickle, pickleStepProcessor);
                    testCaseDefinition.getTestDefinitions().add(cucumberTestBinder.bind());
                });
        return testCaseDefinition;
    }

    private CucumberDocument buildCucumberDocument(Resource resource) {
        try {
            GherkinDocument document = gherkinParser.parse(new InputStreamReader(resource.getInputStream()), tokenMatcher);
            CucumberDocument cucumberDocument = new CucumberDocument();
            cucumberDocument.setDocument(document);
            cucumberDocument.setUrl(resource.getURL());
            return cucumberDocument;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
