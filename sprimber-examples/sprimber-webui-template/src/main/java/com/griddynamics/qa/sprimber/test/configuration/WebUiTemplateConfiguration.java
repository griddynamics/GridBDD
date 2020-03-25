package com.griddynamics.qa.sprimber.test.configuration;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties({WebDriverProperties.class})
@RequiredArgsConstructor
public class WebUiTemplateConfiguration {

    private final WebDriverProperties webDriverProperties;

    @Bean
    public Executor testExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setMaxPoolSize(1);
        taskExecutor.setThreadNamePrefix("TCExecutor-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    @Profile("firefox")
    public WebDriver firefoxDriver() {
        System.setProperty("webdriver.gecko.driver", webDriverProperties.getPathToDriver());
        WebDriver driver = new FirefoxDriver(new FirefoxOptions());
        driver.manage().timeouts().implicitlyWait(webDriverProperties.getLoadWaitInSeconds(), TimeUnit.SECONDS);
        return driver;
    }

    @Bean
    @Profile("chrome")
    public WebDriver chromeDriver() {
        System.setProperty("webdriver.chrome.driver", webDriverProperties.getPathToDriver());
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(webDriverProperties.getLoadWaitInSeconds(), TimeUnit.SECONDS);
        return driver;
    }

    @Bean
    @Profile("edge")
    public WebDriver edgeDriver() {
        System.setProperty("webdriver.edge.driver", webDriverProperties.getPathToDriver());
        WebDriver driver = new EdgeDriver();
        driver.manage().timeouts().implicitlyWait(webDriverProperties.getLoadWaitInSeconds(), TimeUnit.SECONDS);
        return driver;
    }
}
