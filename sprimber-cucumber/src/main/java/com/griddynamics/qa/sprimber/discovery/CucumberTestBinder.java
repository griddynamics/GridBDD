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

import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.stepdefinition.TestMethodRegistry;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.griddynamics.qa.sprimber.engine.Node.*;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor
class CucumberTestBinder {

    public static final String BDD_TAGS_ATTRIBUTE_NAME = "bddTags";
    public static final String LOCATION_ATTRIBUTE_NAME = "location";
    public static final String META_ATTRIBUTE_NAME = "meta";

    private static final String TAG_SYMBOL = "@";
    private static final String TAG_VALUE_SEPARATOR = ":";
    private static final String VALUE_SEPARATOR = ",";
    public static final String TEST_LOCATION_ATTRIBUTE_NAME = "testLocation";

    private final PickleStepFactory pickleStepFactory;
    private final TestMethodRegistry testMethodRegistry;

    void buildAndAddTestNode(Node parentNode, Pickle testCandidate,
                             CucumberSuiteDiscovery.CucumberDocument cucumberDocument) {
        String description = getScenarioDescriptionByTestName(cucumberDocument, testCandidate.getName())
                .map(ScenarioDefinition::getDescription).orElse(testCandidate.getName());
        String testLocation = formatLocation(testCandidate);
        String uniqueName = cucumberDocument.getUrl().toString() + cucumberDocument.getDocument().getFeature().getLocation().getLine() + ":" +
                cucumberDocument.getDocument().getFeature().getLocation().getColumn() +
                cucumberDocument.getDocument().getFeature().getName() + testLocation + testCandidate.getName();
        Builder builder = new Builder()
                .withSubNodeModes(BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_CHILDREN_AFTER_ITERATION_ERROR)
                .withRole("test")
                .withName(testCandidate.getName())
                .withDescription(description)
                .withHistoryId(DigestUtils.md5DigestAsHex(uniqueName.getBytes()))
                .withAttribute(BDD_TAGS_ATTRIBUTE_NAME, getTagsFromPickle(testCandidate))
                .withAttribute(LOCATION_ATTRIBUTE_NAME, testLocation)
                .withAttribute(META_ATTRIBUTE_NAME, getMetaFromPickle(testCandidate))
                .withAttribute(TEST_LOCATION_ATTRIBUTE_NAME, uniqueName);
        Node testNode = parentNode.addChild(builder);

        fillPreConditions("Before Test", testNode);
        fillPostConditions("After Test", testNode);

        testCandidate.getSteps().stream()
                .map(pickleStep -> pickleStepFactory.addStepContainerNode(testNode, pickleStep))
                .forEach(this::fillStepBeforeAndAfter);
    }

    private void fillStepBeforeAndAfter(Node stepContainerNode) {
        fillPreConditions("Before Step", stepContainerNode);
        fillPostConditions("After Step", stepContainerNode);
    }

    private void fillPreConditions(String style, Node containerNode) {
        testMethodRegistry.streamByStyle(style)
                .filter(pickleStepFactory.filterTestMethodByTags())
                .map(testMethod -> new Builder()
                        .withRole(style)
                        .withName(testMethod.getStyle())
                        .withMethod(testMethod.getMethod()))
                .forEach(containerNode::addBefore);
    }

    private void fillPostConditions(String style, Node containerNode) {
        testMethodRegistry.streamByStyle(style)
                .filter(pickleStepFactory.filterTestMethodByTags())
                .map(testMethod -> new Builder()
                        .withRole(style)
                        .withName(testMethod.getStyle())
                        .withMethod(testMethod.getMethod()))
                .forEach(containerNode::addAfter);
    }

    private String formatLocation(Pickle pickle) {
        PickleLocation pickleLocation = pickle.getLocations().get(0);
        return pickleLocation.getLine() + ":" + pickleLocation.getColumn();
    }

    private List<String> getTagsFromPickle(Pickle pickle) {
        return pickle.getTags().stream()
                .map(PickleTag::getName)
                .collect(Collectors.toList());
    }

    private Meta getMetaFromPickle(Pickle pickle) {
        return pickle.getTags().stream()
                .map(PickleTag::getName)
                .map(tag -> StringUtils.remove(tag, TAG_SYMBOL))
                .collect(Meta::new, this::convertValues, HashMap::putAll);
    }

    private void convertValues(Meta meta, String s) {
        StringTokenizer stringTokenizer = new StringTokenizer(s, TAG_VALUE_SEPARATOR);
        String key = stringTokenizer.nextToken();
        if (stringTokenizer.hasMoreTokens()) {
            List<String> values = Arrays.asList(StringUtils.split(stringTokenizer.nextToken(), VALUE_SEPARATOR));
            meta.put(key, values);
        } else {
            meta.put(key, Collections.emptyList());
        }
    }

    private Optional<ScenarioDefinition> getScenarioDescriptionByTestName(CucumberSuiteDiscovery.CucumberDocument cucumberDocument,
                                                                          String name) {
        return cucumberDocument.getDocument().getFeature().getChildren().stream()
                .filter(scenarioDefinition -> name.equals(scenarioDefinition.getName()))
                .findFirst();
    }
}
