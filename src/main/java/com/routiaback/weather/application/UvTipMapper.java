package com.routiaback.weather.application;

public class UvTipMapper {

    private UvTipMapper() {}

    public static String toTip(double uvIndex) {
        if (uvIndex >= 8) return "야외 활동 주의하세요!";
        if (uvIndex >= 6) return "자외선 차단제를 꼭 발라주세요!";
        if (uvIndex >= 3) return "가벼운 자외선 대비가 필요해요.";
        return "자외선 걱정 없는 날이에요!";
    }
}