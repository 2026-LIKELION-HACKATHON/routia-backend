package com.routiaback.weather.domain;

import java.math.BigDecimal;

public record UserLocation(
        BigDecimal latitude,
        BigDecimal longitude,
        String regionSido,
        String regionSigungu
) {}