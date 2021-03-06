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

import com.griddynamics.qa.sprimber.configuration.SprimberProperties;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class CucumberTagFilter implements TagFilter {

    private final SprimberProperties sprimberProperties;
    private final Map<String, Expression> expressionCache = new HashMap<>();
    private List<Expression> expressions = new ArrayList<>();

    @PostConstruct
    void initExpressions() {
        TagExpressionParser tagExpressionParser = new TagExpressionParser();
        expressions.addAll(sprimberProperties.getTagFilters().stream().map(tagExpressionParser::parse).collect(Collectors.toList()));
    }

    @Override
    public boolean filter(List<String> tags) {
        return expressions.stream().allMatch(expression -> expression.evaluate(tags));
    }

    @Override
    public boolean filterByCustomExpression(List<String> tagsToEvaluate, String expressionAsString) {
        expressionCache.computeIfAbsent(expressionAsString, this::buildExpression);
        return expressionCache.get(expressionAsString).evaluate(tagsToEvaluate);
    }

    boolean filter(String tagsAsCsv) {
        List<String> tags = Arrays.asList(StringUtils.tokenizeToStringArray(tagsAsCsv, ","));
        return tags.isEmpty() || filter(tags);
    }

    private Expression buildExpression(String expressionAsString) {
        TagExpressionParser tagExpressionParser = new TagExpressionParser();
        return tagExpressionParser.parse(expressionAsString);
    }
}
