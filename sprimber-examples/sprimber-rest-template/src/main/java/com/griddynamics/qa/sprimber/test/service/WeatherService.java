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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.qa.sprimber.test.model.WeatherResponse;
import com.griddynamics.qa.sprimber.test.repository.WeatherClient;
import com.griddynamics.qa.sprimber.test.storage.WeatherStorage;
import io.qameta.allure.AllureLifecycle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * @author pmichalski
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private static final String EMPTY_RESPONSE_BODY = "Empty response body";

    private final WeatherClient weatherClient;
    private final WeatherStorage weatherStorage;
    private final AllureLifecycle allureLifecycle;
    private final ObjectMapper objectMapper;

    public void getCurrentWeather(String requestId, String country, String city) {
        log.debug("Calling weather service. RequestId: {}, Country: {}, City: {}", requestId, country, city);
        Call<WeatherResponse> weatherResponseCall = weatherClient.getCurrentWeather(country, city);
        try {
            Response<WeatherResponse> weatherResponse = weatherResponseCall.execute();
            weatherStorage.getWeatherResponseMap().put(requestId, weatherResponse);
            allureLifecycle.addAttachment(
                    String.format("Weather service response for  RequestId: %s, Country: %s, City: %s", requestId, country, city),
                    "application/json",
                    "json",
                    Objects.nonNull(weatherResponse.body()) ? objectMapper.writeValueAsBytes(weatherResponse.body()) : EMPTY_RESPONSE_BODY.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Exception during call to current weather endpoint", e);
        }
    }
}
