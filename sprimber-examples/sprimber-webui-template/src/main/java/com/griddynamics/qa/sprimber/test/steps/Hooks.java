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
