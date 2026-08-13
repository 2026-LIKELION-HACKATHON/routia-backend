package com.routiaback.weather.application;

public class WeatherCodeMapper {

    private WeatherCodeMapper() {}

    public static String toDescription(int code) {
        if (code == 0) return "맑음";
        if (code >= 1 && code <= 3) return "구름 조금";
        if (code >= 45 && code <= 48) return "안개";
        if (code >= 51 && code <= 67) return "비";
        if (code >= 71 && code <= 77) return "눈";
        if (code >= 80 && code <= 82) return "소나기";
        if (code >= 95) return "뇌우";
        return "알 수 없음";
    }
}