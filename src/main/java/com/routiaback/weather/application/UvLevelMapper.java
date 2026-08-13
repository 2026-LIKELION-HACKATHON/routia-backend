package com.routiaback.weather.application;

public class UvLevelMapper {

    private UvLevelMapper() {}

    public static String toLevel(double uvIndex) {
        if (uvIndex < 3) return "낮음";
        if (uvIndex < 6) return "보통";
        if (uvIndex < 8) return "높음";
        if (uvIndex < 11) return "매우 높음";
        return "위험";
    }
}