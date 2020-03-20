package com.griddynamics.qa.sprimber.test.model;

import com.griddynamics.qa.sprimber.test.configuration.WebDriverProperties;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@Component
public class GetInTouchModel extends PageModel {

    private final static String EXPECTED_WINDOW_TITLE = "Contact Us | Grid Dynamics";

    public GetInTouchModel(WebDriver webDriver, WebDriverProperties webDriverProperties) {
        super(webDriver, webDriverProperties);
    }

    public boolean isPageLoaded() {
        return super.isPageLoaded(EXPECTED_WINDOW_TITLE);
    }
}
