package com.routiaback.weather.application.port;

import com.routiaback.weather.domain.WeatherInfo;

public interface WeatherClientPort {
    WeatherInfo fetchCurrentWeather(double latitude, double longitude);
}