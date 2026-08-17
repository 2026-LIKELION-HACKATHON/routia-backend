package com.routiaback.weather.application.result;

public record WeatherTodayResult(
        String regionSido,
        String regionSigungu,
        double temperature,
        double feelsLike,
        String weatherDescription,
        String temperatureTip,
        double uvIndex,
        String uvLevel,
        String uvTip
) {}