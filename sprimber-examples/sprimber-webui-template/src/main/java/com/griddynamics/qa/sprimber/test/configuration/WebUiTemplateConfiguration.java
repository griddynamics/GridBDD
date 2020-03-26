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

/**
 * @author pmichalski
 */

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
