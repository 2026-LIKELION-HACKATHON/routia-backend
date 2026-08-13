package com.routiaback.weather.presentation;

import com.routiaback.global.common.apiResponse.ApiResponse;
import com.routiaback.weather.application.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "날씨", description = "날씨/자외선 정보 API")
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "오늘 날씨/자외선 정보 조회")
    @GetMapping("/today")
    public ApiResponse<WeatherResponse> getToday(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(WeatherResponse.from(weatherService.getToday(userId)));
    }
}