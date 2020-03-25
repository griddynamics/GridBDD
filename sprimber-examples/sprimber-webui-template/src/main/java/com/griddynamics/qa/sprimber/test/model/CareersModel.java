package com.griddynamics.qa.sprimber.test.model;

import com.griddynamics.qa.sprimber.test.configuration.WebDriverProperties;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@Component
public class CareersModel extends PageModel {

    private final static String EXPECTED_WINDOW_TITLE = "Careers at Grid Dynamics";

    public CareersModel(WebDriver webDriver, WebDriverProperties webDriverProperties) {
        super(webDriver, webDriverProperties);
    }

    public boolean isPageLoaded() {
        return super.isPageLoaded(EXPECTED_WINDOW_TITLE);
    }
}
