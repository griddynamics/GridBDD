package com.griddynamics.qa.sprimber.test.model;

import com.griddynamics.qa.sprimber.test.configuration.WebDriverProperties;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@RequiredArgsConstructor
public abstract class PageModel {
    private final WebDriver webDriver;
    private final WebDriverProperties webDriverProperties;

    private static final String XPATH_BUTTON_GET_IN_TOUCH = "//span[text()='Get in touch']";
    private static final String XPATH_LINK_CAREERS = "//header//a[text()='Careers']";

    public void navigateToGetInTouch() {
        this.clickBy(By.xpath(XPATH_BUTTON_GET_IN_TOUCH));
    }

    public void navigateToCareers() {
        this.clickBy(By.xpath(XPATH_LINK_CAREERS));
    }

    protected boolean isPageLoaded(String expectedPageTitle) {
        return new WebDriverWait(webDriver, webDriverProperties.getLoadWaitInSeconds()).until(ExpectedConditions.titleIs(expectedPageTitle));
    }

    protected void clickBy(By by) {
        WebElement webElement = new WebDriverWait(webDriver, webDriverProperties.getLoadWaitInSeconds())
                .until(ExpectedConditions.elementToBeClickable(by));
        webElement.click();
    }
}
