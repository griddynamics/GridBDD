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

package com.griddynamics.qa.sprimber.test.steps;

import com.griddynamics.qa.sprimber.condition.SkipOnProperty;
import com.griddynamics.qa.sprimber.engine.model.action.Actions;
import com.griddynamics.qa.sprimber.test.model.Author;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author fparamonov
 */
@Actions
public class BaseEchoSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEchoSteps.class);

    public BaseEchoSteps() {
    }

    @Before
    public void before() {
        LOGGER.info("Hey, I'm before action");
    }

    @BeforeStep
    public void beforeStep() {
        LOGGER.info("Hey, I'm beforeStep action");
    }

    @Given("^test given action$")
    public void given() {
        LOGGER.info("Hey, I'm given action");
    }

    @Given("the next author exist:")
    public void givenDataTable(List<Author> authors) {
        LOGGER.info("Hey, I'm given action for author {}", authors.get(0));
        Assertions.assertThat(authors).hasSize(1);
        Assertions.assertThat(authors).extracting("name").contains("testName");
    }

    @Given("the next raw data table present")
    public void givenRawDataTable(DataTable dataTable) {
        LOGGER.info("Hey, I'm given action for author {}", dataTable.cell(0,0));
    }

    @Given("the next author long consumed '{long}'")
    public void givenLong(Long dummyNumber) {
        LOGGER.info("Hey, I'm given action with long param {}", dummyNumber);
        Assertions.assertThat(dummyNumber).isEqualTo(123L);
    }

    @Given("the next author long consumed '{long}' and '{long}'")
    public void givenTwoLong(Long dummyNumber, Long secondNumber) {
        LOGGER.info("Hey, I'm given action with long param {} and {}", dummyNumber, secondNumber);
        Assertions.assertThat(dummyNumber).isEqualTo(123L);
        Assertions.assertThat(secondNumber).isEqualTo(321L);
    }

    @When("^test when action$")
    public void when() {
        LOGGER.info("Hey, I'm when action");
    }

    @When("some when action with param '{word}'")
    public void whenWithParameters(String word) {
        LOGGER.info("Hey, I'm when action with param {}", word);
    }

    @SkipOnProperty(value = "skip.me.when.you.want", havingValue = "skip")
    @When("conditional action")
    public void whenThatMayBeSkipped() {
        LOGGER.info("I'm step that can be conditionally skipped");
    }

    @Then("test then action")
    public void then() {
        LOGGER.info("Hey, I'm then action");
    }

    @Then("every time failed action")
    public void thenFail() {
        LOGGER.info("Hey, I'm every time failed action");
        Assertions.assertThat("true").as("True should never be false").isEqualTo("false");
    }

    @Then("every time action with exception")
    public void thenException() {
        LOGGER.info("Hey, I'm every time failed action");
        throw new RuntimeException("Everything is broken");
    }

    @AfterStep
    public void afterStep() {
        LOGGER.info("Hey, I'm afterStep action");
    }

    @After
    public void after() {
        LOGGER.info("Hey, I'm after action");
    }

    public void dummy() {
        LOGGER.info("Hey, I'm not an action");
    }
}
