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

package com.griddynamics.qa.sprimber.engine.processor.cucumber;

import com.griddynamics.qa.sprimber.engine.model.TestCase;
import com.griddynamics.qa.sprimber.engine.configuration.SprimberProperties;
import com.griddynamics.qa.sprimber.engine.processor.ResourceProcessor;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTag;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This implementation of common resource processor interface aimed to resolve Gherkin based features
 * to Sprimber BDD Java abstraction
 *
 * @author fparamonov
 */

@Component
public class CucumberProcessor implements ResourceProcessor {

    private final Parser<GherkinDocument> gherkinParser;
    private final TokenMatcher tokenMatcher;
    private final Compiler compiler;
    private final SprimberProperties sprimberProperties;
    private final ApplicationContext applicationContext;
    private final PickleProcessor pickleProcessor;

    public CucumberProcessor(Parser<GherkinDocument> gherkinParser,
                             TokenMatcher tokenMatcher,
                             Compiler compiler,
                             SprimberProperties sprimberProperties,
                             ApplicationContext applicationContext,
                             PickleProcessor pickleProcessor) {
        this.gherkinParser = gherkinParser;
        this.tokenMatcher = tokenMatcher;
        this.compiler = compiler;
        this.sprimberProperties = sprimberProperties;
        this.pickleProcessor = pickleProcessor;
        this.applicationContext = applicationContext;
    }

    @Override
    public List<TestCase> process() {
        List<TestCase> testCases = new ArrayList<>();
        try {
            testCases = Arrays.stream(applicationContext.getResources(sprimberProperties.getFeaturePath()))
                    .map(this::buildCucumberDocument)
                    .flatMap(this::buildTestCasesStream)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testCases;
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

    private Stream<TestCase> buildTestCasesStream(CucumberDocument cucumberDocument) {
        return compiler.compile(cucumberDocument.getDocument()).stream()
                .filter(pickleTagPredicate())
                .map(pickle -> pickleProcessor.processPickle(cucumberDocument, pickle));
    }

    private Predicate<Pickle> pickleTagPredicate() {
        return pickle -> {
            TagFilter tagFilter = new TagFilter(sprimberProperties.getTagFilters());
            return tagFilter.filter(getTagsForPickle(pickle));
        };
    }

    private List<String> getTagsForPickle(Pickle pickle) {
        return pickle.getTags().stream()
                .map(PickleTag::getName)
                .collect(Collectors.toList());
    }
}
