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

import com.griddynamics.qa.sprimber.discovery.Actions;
import com.griddynamics.qa.sprimber.test.service.WeatherService;
import com.griddynamics.qa.sprimber.test.service.WeatherValidationService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

import static com.griddynamics.qa.sprimber.test.configuration.DataTableFields.*;

/**
 * @author pmichalski
 */

@Actions
@RequiredArgsConstructor
public class WeatherSteps {

    private final WeatherService weatherService;
    private final WeatherValidationService weatherValidationService;

    @Given("^Weather rest service is called with following values:$")
    public void getCurrentWeather(DataTable dataTable) {
        dataTable.asMaps()
                .forEach(rowAsMap -> weatherService.getCurrentWeather(
                        rowAsMap.get(REQUEST_ID),
                        rowAsMap.get(COUNTRY),
                        rowAsMap.get(CITY)
                ));
    }

    @When("^following calls are successful:$")
    public void getCurrentWeatherCallIsSuccessful(DataTable dataTable) {
        dataTable.asMaps()
                .forEach(rowAsMap -> weatherValidationService.isCallSuccessful(rowAsMap.get(REQUEST_ID)));
    }

    @When("^following calls are failed with status code:$")
    public void getCurrentWeatherCallIsFailedWithStatusCode(DataTable dataTable) {
        dataTable.asMaps()
                .forEach(rowAsMap ->
                        weatherValidationService.isCallFailedWithStatusCode(
                                rowAsMap.get(REQUEST_ID),
                                Integer.valueOf(rowAsMap.get(STATUS_CODE)))
                );
    }

    @Then("^temperature and humidity values are present for calls:$")
    public void validateGetCurrentWeatherResponse(DataTable dataTable) {
        dataTable.asMaps()
                .forEach(rowAsMap -> weatherValidationService.areValuesPresentInCurrentWeatherResponse(rowAsMap.get(REQUEST_ID)));
    }
}
