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
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@Component
public class PickleProcessor {

    private final PickleStepProcessor pickleStepProcessor;
    private final PickleHookProcessor pickleHookProcessor;

    public PickleProcessor(PickleStepProcessor pickleStepProcessor,
                           PickleHookProcessor pickleHookProcessor) {
        this.pickleStepProcessor = pickleStepProcessor;
        this.pickleHookProcessor = pickleHookProcessor;
    }

    public TestCase processPickle(CucumberDocument cucumberDocument, Pickle pickle) {
        TestCase testCase = new TestCase();

        testCase.getAllHooks().addAll(pickleHookProcessor.processPickleHook(pickle));
        testCase.getSteps().addAll(
                pickle.getSteps().stream()
                        .map(pickleStepProcessor::processPickleStep)
                        .collect(Collectors.toList())
        );

        testCase.getTags().addAll(getTagsForPickle(pickle));
        testCase.setName(pickle.getName());
        testCase.setLocation(buildLocationString(pickle));

        TestCase.Parent parent = new TestCase.Parent();
        parent.setName(cucumberDocument.getDocument().getFeature().getName());
        parent.setDescription(cucumberDocument.getDocument().getFeature().getDescription());
        parent.setUrl(cucumberDocument.getUrl().getPath());
        testCase.setParent(parent);

        testCase.setRuntimeId(UUID.randomUUID().toString());
        return testCase;
    }

    private String buildLocationString(Pickle pickle) {
        PickleLocation pickleLocation = pickle.getLocations().get(0);
        return pickleLocation.getLine() + ":" + pickleLocation.getColumn();
    }

    private List<String> getTagsForPickle(Pickle pickle) {
        return pickle.getTags().stream()
                .map(PickleTag::getName)
                .collect(Collectors.toList());
    }
}