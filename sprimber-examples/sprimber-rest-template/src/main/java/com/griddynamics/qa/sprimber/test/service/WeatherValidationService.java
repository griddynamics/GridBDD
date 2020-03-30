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

package com.griddynamics.qa.sprimber.test.service;

import com.griddynamics.qa.sprimber.test.model.WeatherResponse;
import com.griddynamics.qa.sprimber.test.storage.WeatherStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author pmichalski
 */

@Component
@RequiredArgsConstructor
public class WeatherValidationService {

    private final WeatherStorage weatherStorage;

    public void isCallSuccessful(String requestId) {
        Response<WeatherResponse> weatherResponse = weatherStorage.getWeatherResponseMap().get(requestId);
        assertThat(weatherResponse)
                .as(String.format("Weather response for requestId %s is null", requestId))
                .isNotNull();
        assertThat(weatherResponse.code())
                .as("Weather response status code should be 200")
                .isEqualTo(200);
        assertThat(weatherResponse.body())
                .as(String.format("Weather response body for requestId %s is null", requestId))
                .isNotNull();
    }

    public void isCallFailedWithStatusCode(String requestId, Integer statusCode) {
        Response<WeatherResponse> weatherResponse = weatherStorage.getWeatherResponseMap().get(requestId);
        assertThat(weatherResponse)
                .as(String.format("Weather response for requestId %s is null", requestId))
                .isNotNull();
        assertThat(weatherResponse.code())
                .as(String.format("Weather response status code should be %d", statusCode))
                .isEqualTo(statusCode);
    }

    public void areValuesPresentInCurrentWeatherResponse(String requestId) {
        Response<WeatherResponse> weatherResponse = weatherStorage.getWeatherResponseMap().get(requestId);
        assertThat(weatherResponse.body().getHumidity())
                .as(String.format("Weather response for requestId %s humidity is not present", requestId))
                .isNotNull();
        assertThat(weatherResponse.body().getTemperature())
                .as(String.format("Weather response for requestId %s temperature is not present", requestId))
                .isNotNull();
    }
}
