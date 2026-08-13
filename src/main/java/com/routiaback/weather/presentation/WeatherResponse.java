package com.routiaback.weather.presentation;

import com.routiaback.weather.application.result.WeatherTodayResult;

public record WeatherResponse(
        String regionSido,
        String regionSigungu,
        double temperature,
        double feelsLike,
        String weatherDescription,
        String temperatureTip,
        double uvIndex,
        String uvLevel,
        String uvTip
) {
    public static WeatherResponse from(WeatherTodayResult result) {
        return new WeatherResponse(
                result.regionSido(), result.regionSigungu(), result.temperature(), result.feelsLike(),
                result.weatherDescription(), result.temperatureTip(),
                result.uvIndex(), result.uvLevel(), result.uvTip()
        );
    }
}