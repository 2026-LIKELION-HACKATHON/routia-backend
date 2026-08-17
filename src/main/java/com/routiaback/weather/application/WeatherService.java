package com.routiaback.weather.application;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.weather.application.port.UserLocationPort;
import com.routiaback.weather.application.port.WeatherClientPort;
import com.routiaback.weather.application.result.WeatherTodayResult;
import com.routiaback.weather.domain.UserLocation;
import com.routiaback.weather.domain.WeatherInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final UserLocationPort userLocationPort;
    private final WeatherClientPort weatherClientPort;

    public WeatherTodayResult getToday(Long userId) {
        UserLocation location = userLocationPort.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_LOCATION_NOT_FOUND));

        WeatherInfo weather = weatherClientPort.fetchCurrentWeather(
                location.latitude().doubleValue(),
                location.longitude().doubleValue()
        );

        return new WeatherTodayResult(
                location.regionSido(),
                location.regionSigungu(),
                weather.temperature(),
                weather.feelsLike(),
                WeatherCodeMapper.toDescription(weather.weatherCode()),
                TemperatureTipMapper.toTip(weather.feelsLike()),
                weather.uvIndex(),
                UvLevelMapper.toLevel(weather.uvIndex()),
                UvTipMapper.toTip(weather.uvIndex())
        );
    }
}