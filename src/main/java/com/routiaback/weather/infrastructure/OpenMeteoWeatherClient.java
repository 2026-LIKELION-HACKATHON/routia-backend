package com.routiaback.weather.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.routiaback.weather.application.port.WeatherClientPort;
import com.routiaback.weather.domain.WeatherInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
class OpenMeteoWeatherClient implements WeatherClientPort {

    private final RestClient restClient = RestClient.create("https://api.open-meteo.com");

    @Override
    public WeatherInfo fetchCurrentWeather(double latitude, double longitude) {
        OpenMeteoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,apparent_temperature,weather_code")
                        .queryParam("daily", "uv_index_max")
                        .queryParam("timezone", "Asia/Seoul")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        return new WeatherInfo(
                response.current().temperature2m(),
                response.current().apparentTemperature(),
                response.current().weatherCode(),
                response.daily().uvIndexMax().get(0)
        );
    }

    private record OpenMeteoResponse(Current current, Daily daily) {
        private record Current(
                @JsonProperty("temperature_2m") double temperature2m,
                @JsonProperty("apparent_temperature") double apparentTemperature,
                @JsonProperty("weather_code") int weatherCode
        ) {}

        private record Daily(
                @JsonProperty("uv_index_max") List<Double> uvIndexMax
        ) {}
    }
}