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

import com.griddynamics.qa.sprimber.engine.model.action.Actions;
import com.griddynamics.qa.sprimber.reporting.SprimberEventPublisher;
import io.cucumber.java.AfterStep;
import io.qameta.allure.AllureLifecycle;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.context.event.EventListener;

/**
 * @author pmichalski
 */

@Actions
@RequiredArgsConstructor
public class Hooks {

    private final AllureLifecycle allureLifecycle;
    private final WebDriver webDriver;

    @AfterStep
    public void attachScreenShot() {
        allureLifecycle.addAttachment("Screenshot after test step", "image/png", "png",
                ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES));
    }

    @EventListener
    public void after(SprimberEventPublisher.TargetNodeErrorEvent errorEvent) {
        allureLifecycle.addAttachment("Page source after failure", "text/plain", "html",
                webDriver.getPageSource().getBytes());
    }
}
