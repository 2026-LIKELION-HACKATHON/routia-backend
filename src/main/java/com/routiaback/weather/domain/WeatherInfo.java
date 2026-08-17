package com.routiaback.weather.domain;

public record WeatherInfo(
        double temperature,
        double feelsLike,
        int weatherCode,
        double uvIndex
) {}