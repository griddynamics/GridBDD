package com.griddynamics.qa.sprimber.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebUiTemplate {

    public static void main(String[] args) throws Exception {
        SpringApplication.exit(SpringApplication.run(WebUiTemplate.class));
    }
}
