package com.griddynamics.qa.sprimber.test.steps;

import com.griddynamics.qa.sprimber.engine.model.action.Actions;
import com.griddynamics.qa.sprimber.test.model.GetInTouchModel;
import cucumber.api.java.en.Then;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@Actions
@RequiredArgsConstructor
public class GetInTouchSteps {

    private final GetInTouchModel getInTouchModel;

    @Then("'Get in Touch' page is opened")
    public void getInTouchIsOpened() {
        assertThat(getInTouchModel.isPageLoaded()).isTrue();
    }
}
