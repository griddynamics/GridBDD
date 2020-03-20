package com.griddynamics.qa.sprimber.test.configuration;

import com.griddynamics.qa.sprimber.scope.ScenarioScope;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties({WebDriverProperties.class})
@RequiredArgsConstructor
public class WebUiTemplateConfiguration {

    private final WebDriverProperties webDriverProperties;

    @Bean
    @ConditionalOnProperty(prefix = "web-driver", value = "driverType", havingValue = "firefox")
    @ScenarioScope
    public WebDriver webDriver() {
        System.setProperty("webdriver.gecko.driver", webDriverProperties.getPathToDriver());
        WebDriver driver = new FirefoxDriver(new FirefoxOptions());
        driver.manage().timeouts().implicitlyWait(webDriverProperties.getLoadWaitInSeconds(), TimeUnit.SECONDS);
        return driver;
    }

}
