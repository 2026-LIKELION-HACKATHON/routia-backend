package com.routiaback.routine.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record WeatherSnapshot(Long id, Long userId, LocalDate targetDate, String regionSido,
        String regionSigungu, BigDecimal latitude, BigDecimal longitude, BigDecimal temperature,
        BigDecimal uvIndex, String weatherCondition, String provider, Instant observedAt, Instant createdAt) { }
