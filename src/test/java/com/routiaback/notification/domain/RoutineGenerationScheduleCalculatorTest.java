package com.routiaback.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.*;
import org.junit.jupiter.api.Test;

class RoutineGenerationScheduleCalculatorTest {
    private final RoutineGenerationScheduleCalculator calculator=new RoutineGenerationScheduleCalculator();
    @Test void schedulesTenMinutesBeforeNotification(){var target=calculator.next(LocalTime.of(8,0),"Asia/Seoul",Instant.parse("2026-08-14T20:00:00Z"));assertThat(target.routineDate()).isEqualTo(LocalDate.of(2026,8,15));assertThat(target.generationAt()).isEqualTo(Instant.parse("2026-08-14T22:50:00Z"));assertThat(target.notificationAt()).isEqualTo(Instant.parse("2026-08-14T23:00:00Z"));}
    @Test void preservesTargetDateAcrossMidnightBoundary(){var target=calculator.next(LocalTime.of(0,5),"Asia/Seoul",Instant.parse("2026-08-15T14:00:00Z"));assertThat(target.routineDate()).isEqualTo(LocalDate.of(2026,8,16));assertThat(target.generationAt()).isEqualTo(Instant.parse("2026-08-15T14:55:00Z"));assertThat(target.notificationAt()).isEqualTo(Instant.parse("2026-08-15T15:05:00Z"));}
}
