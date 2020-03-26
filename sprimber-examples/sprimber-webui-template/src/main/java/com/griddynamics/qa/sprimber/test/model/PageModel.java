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

package com.griddynamics.qa.sprimber.test.model;

import com.griddynamics.qa.sprimber.test.configuration.WebDriverProperties;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author pmichalski
 */

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
