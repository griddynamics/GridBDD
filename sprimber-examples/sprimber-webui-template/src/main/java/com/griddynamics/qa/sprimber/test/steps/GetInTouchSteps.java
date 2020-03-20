package com.griddynamics.qa.sprimber.test.steps;

import com.griddynamics.qa.sprimber.engine.model.action.Actions;
import com.griddynamics.qa.sprimber.test.model.GetInTouchModel;
import cucumber.api.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Actions
@RequiredArgsConstructor
@Slf4j
public class GetInTouchSteps {

    private final GetInTouchModel getInTouchModel;

    @Then("'Get in Touch' page is opened")
    public void getInTouchIsOpened() {
        assertThat(getInTouchModel.isPageLoaded()).isTrue();
    }
}
