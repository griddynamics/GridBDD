# sprimber
[![Maven Central](https://img.shields.io/maven-central/v/com.griddynamics.qa/sprimber-parent.svg?color=green&style=for-the-badge)](https://search.maven.org/search?q=g:%22com.griddynamics.qa%22%20AND%20a:%22sprimber-spring-boot-starter%22)
![](https://img.shields.io/github/workflow/status/griddynamics/GridBDD/Java%20CI/master?color=green&label=Master%20CI&style=for-the-badge)

Cloud ready and customiaztion friendly BDD-style test automation framework that support multithreading.

Sprimber provides most popoular BDD Gherkin-style interface on top of configurable workflow engine. Under the hood engine built with power from Spring and Spring Boot technologies that allow to embed Sprimber simply it any existing/new Spring Boot application as regular spring-boot-starter. 
All components inside of Sprimber presents as a beans in the context that allow to simply modify and extand them.

Sprimber designed for integrated and end-2-end test automation, so that test automation solutions written on Sprimber can be deployed to the cloud environments and interact with all Cloud infrastructure like Service Discovery. Also native Spring integration allows to simplify and conjoin DevOps processes for application and test automation code.

# Key Features
* Not a pure Java implementation, strong usage Spring projects
* Works perfect only at Java and Spring basement 
* But!
* Natively support multithreading via Spring Task abstraction
* Support Spring bean scopes to handle different state between steps in scope of one test case
* Can understand steps and step definitions from popular BDD frameworks like JBehave and Cucumber(Gherkin)
* Default reporting in allure format

# More Information
Follow guides at [wiki page](https://github.com/griddynamics/GridBDD/wiki/Building-a-Test-Automation-with-Spring-Boot-BDD)
