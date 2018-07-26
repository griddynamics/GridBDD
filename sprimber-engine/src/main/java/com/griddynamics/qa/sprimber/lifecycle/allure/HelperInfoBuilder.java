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

package com.griddynamics.qa.sprimber.lifecycle.allure;

import com.griddynamics.qa.sprimber.engine.model.TestCase;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.util.ResultsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.qameta.allure.util.ResultsUtils.*;

/**
 * @author fparamonov
 */
public class HelperInfoBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelperInfoBuilder.class);

    private static final String COMPOSITE_TAG_DELIMITER = "=";

    private static final String SEVERITY = "@SEVERITY";
    private static final String ISSUE_LINK = "@ISSUE";
    private static final String TMS_LINK = "@TMSLINK";
    private static final String PLAIN_LINK = "@LINK";

    private final TestCase testCase;


    public HelperInfoBuilder(TestCase testCase) {
        this.testCase = testCase;
    }

    public List<Label> getLabels() {
        List<Label> labels = new ArrayList<>();
        TagParser tagParser = new TagParser(testCase);
        labels.add(ResultsUtils.createFeatureLabel(testCase.getParentName()));
        labels.add(ResultsUtils.createStoryLabel(testCase.getName()));

        keyValueFromScenario()
                .filter(tagKeyValue -> SEVERITY.equals(tagKeyValue.getKey()))
                .forEach(tagKeyValue -> createSeverityLabel(tagKeyValue.getValue().toLowerCase()));
        testCase.getTags().stream()
                .filter(tagParser::isPureSeverityTag)
                .forEach(tag -> labels.add(createSeverityLabel(tag.substring(1))));
        testCase.getTags().stream()
                .filter(tagParser::isResultTag)
                .forEach(tag -> labels.add(createTagLabel(tag.substring(1))));

        labels.add(new Label().withName("host").withValue(getHostName()));
        labels.add(new Label().withName("package").withValue(testCase.getParentName()));
        labels.add(new Label().withName("suite").withValue(testCase.getParentName()));
        labels.add(new Label().withName("testClass").withValue(testCase.getName()));
        labels.add(new Label().withName("thread").withValue(getThreadName()));
        return labels;
    }

    public List<Link> getLinks() {
        List<Link> links = new ArrayList<>();
        keyValueFromScenario()
                .filter(tagKeyValue -> tagKeyValue.getValue().startsWith(PLAIN_LINK + "."))
                .forEach(tagKeyValue -> tryHandleNamedLink(links, tagKeyValue.getRawValue()));
        keyValueFromScenario()
                .filter(tagKeyValue -> TMS_LINK.equals(tagKeyValue.getKey()))
                .forEach(tagKeyValue -> links.add(createTmsLink(tagKeyValue.value)));
        keyValueFromScenario()
                .filter(tagKeyValue -> ISSUE_LINK.equals(tagKeyValue.getKey()))
                .forEach(tagKeyValue -> links.add(createIssueLink(tagKeyValue.value)));
        keyValueFromScenario()
                .filter(tagKeyValue -> PLAIN_LINK.equals(tagKeyValue.getKey()))
                .forEach(tagKeyValue -> links.add(createLink(null, tagKeyValue.getKey(), tagKeyValue.getValue(), null)));
        return links;
    }

    private Stream<TagKeyValue> keyValueFromScenario() {
        return testCase.getTags().stream()
                .filter(tag -> tag.contains(COMPOSITE_TAG_DELIMITER))
                .map(TagKeyValue::new)
                .filter(TagKeyValue::isValueNotEmpty);
    }

    /**
     * Handle composite named links.
     *
     * @param tagString Full tag name and value
     */
    private void tryHandleNamedLink(List<Link> links, String tagString) {
        final String namedLinkPatternString = PLAIN_LINK + "\\.(\\w+-?)+=(\\w+(-|_)?)+";
        final Pattern namedLinkPattern = Pattern.compile(namedLinkPatternString, Pattern.CASE_INSENSITIVE);

        if (namedLinkPattern.matcher(tagString).matches()) {
            final String type = tagString.split(COMPOSITE_TAG_DELIMITER)[0].split("[.]")[1];
            final String name = tagString.split(COMPOSITE_TAG_DELIMITER)[1];
            links.add(createLink(null, name, null, type));
        } else {
            LOGGER.warn("Composite named tag {} is not matches regex {}. skipping", tagString,
                    namedLinkPatternString);
        }
    }

    // TODO: 12/06/2018 since meta tags is popular in the BDD world think about best location of this class
    private class TagKeyValue {
        private String rawValue;
        private String key;
        private String value;

        public TagKeyValue(String rawValue) {
            String[] keyValue = rawValue.split(COMPOSITE_TAG_DELIMITER, 2);
            this.rawValue = rawValue;
            this.key = keyValue[0];
            this.value = keyValue[1];
        }

        public boolean isValueNotEmpty() {
            return !value.isEmpty();
        }

        public String getRawValue() {
            return rawValue;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
