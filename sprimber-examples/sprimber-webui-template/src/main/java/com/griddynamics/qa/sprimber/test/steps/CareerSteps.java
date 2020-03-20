package com.griddynamics.qa.sprimber.test.steps;

import com.griddynamics.qa.sprimber.engine.model.action.Actions;
import com.griddynamics.qa.sprimber.test.model.CareersModel;
import cucumber.api.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Actions
@RequiredArgsConstructor
@Slf4j
public class CareerSteps {

    private final CareersModel careersModel;

    @Then("'Careers' page is opened")
    public void getInTouchIsOpened() {
        assertThat(careersModel.isPageLoaded()).isTrue();
    }
}
