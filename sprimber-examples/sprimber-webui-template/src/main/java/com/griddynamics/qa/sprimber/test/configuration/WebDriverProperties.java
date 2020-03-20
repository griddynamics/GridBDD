package com.griddynamics.qa.sprimber.test.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("web-driver")
@Data
public class WebDriverProperties {

    private String pathToDriver;
    private Long loadWaitInSeconds;

}
