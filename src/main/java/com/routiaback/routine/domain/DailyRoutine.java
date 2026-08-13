package com.routiaback.routine.domain;

import java.time.Instant;
import java.time.LocalDate;

public record DailyRoutine(
        Long id,
        Long userId,
        LocalDate routineDate,
        String status,
        String directionText,
        String homeComment,
        Instant createdAt,
        Instant updatedAt
) {}