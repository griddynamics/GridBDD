package com.griddynamics.qa.sprimber.test.model;

import com.griddynamics.qa.sprimber.test.configuration.WebDriverProperties;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

@Component
public class MainPageModel extends PageModel {

    private final WebDriver webDriver;

    private static final String MAIN_PAGE_URL = "https://www.griddynamics.com/";

    public MainPageModel(WebDriver webDriver, WebDriverProperties webDriverProperties) {
        super(webDriver, webDriverProperties);

        this.webDriver = webDriver;
    }

    public void navigateTo() {
        webDriver.navigate().to(MAIN_PAGE_URL);
    }
}
