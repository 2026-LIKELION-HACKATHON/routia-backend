package com.routiaback.weather.application;

public class TemperatureTipMapper {

    private TemperatureTipMapper() {}

    public static String toTip(double feelsLike) {
        if (feelsLike >= 33) return "폭염 주의! 야외 활동을 자제하세요.";
        if (feelsLike >= 28) return "수분 보충에 신경쓰세요!";
        if (feelsLike <= 0) return "한랭질환 주의! 따뜻하게 입으세요.";
        if (feelsLike <= 10) return "쌀쌀해요, 겉옷을 챙기세요!";
        return "야외 활동하기 좋은 날씨예요!";
    }
}