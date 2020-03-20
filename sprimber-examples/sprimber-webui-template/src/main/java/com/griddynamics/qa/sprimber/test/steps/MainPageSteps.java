package com.griddynamics.qa.sprimber.test.steps;

import com.griddynamics.qa.sprimber.engine.model.action.Actions;
import com.griddynamics.qa.sprimber.test.model.MainPageModel;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Actions
@RequiredArgsConstructor
@Slf4j
public class MainPageSteps {

    private final MainPageModel mainPageModel;

    @Given("open main GridDynamics page")
    public void loadMainPage() {
        mainPageModel.navigateTo();
    }

    @When("navigate to 'Get in Touch'")
    public void navigateToGetInTouch() {
        mainPageModel.navigateToGetInTouch();
    }

    @When("navigate to 'Careers'")
    public void navigateToCareers() {
        mainPageModel.navigateToCareers();
    }
}
