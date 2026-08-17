package com.routiaback.routine.application.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.*;
import com.routiaback.routine.domain.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class RoutineGenerationSchedulerTest {
    @Test void isolatesUserFailureAndAdvancesEveryDueSchedule(){Instant now=Instant.parse("2026-08-15T00:00:00Z");RoutineScheduleRepositoryPort schedules=org.mockito.Mockito.mock(RoutineScheduleRepositoryPort.class);RoutineGenerationService generation=org.mockito.Mockito.mock(RoutineGenerationService.class);RoutineSchedule a=RoutineSchedule.create(1L,LocalTime.of(9,10),now,now.minusSeconds(60));RoutineSchedule b=RoutineSchedule.create(2L,LocalTime.of(9,10),now,now.minusSeconds(60));given(schedules.findDueActive(now)).willReturn(List.of(a,b));given(generation.generate(eq(1L),any(),eq(RoutineGenerationType.SCHEDULED_DAILY),any())).willThrow(new IllegalStateException("AI failed"));given(generation.generate(eq(2L),any(),eq(RoutineGenerationType.SCHEDULED_DAILY),any())).willReturn(new RoutineGenerationService.GenerationOutcome(2L,RoutineStatus.READY,true));
        List<RoutineSchedule> saved=new ArrayList<>();given(schedules.save(any())).willAnswer(i->{saved.add(i.getArgument(0));return i.getArgument(0);});
        new RoutineGenerationScheduler(schedules,generation,new RoutineGenerationScheduleCalculator(),Clock.fixed(now,ZoneOffset.UTC)).generateDue();
        then(generation).should().generate(eq(2L),eq(LocalDate.of(2026,8,15)),eq(RoutineGenerationType.SCHEDULED_DAILY),eq(Instant.parse("2026-08-15T00:10:00Z")));assertThat(saved).hasSize(2).allMatch(s->s.nextGenerationAt().equals(Instant.parse("2026-08-16T00:00:00Z")));
    }

    @Test void generatesWithoutNotificationForUnconfiguredUser(){Instant now=Instant.parse("2026-08-15T21:00:00Z");RoutineScheduleRepositoryPort schedules=org.mockito.Mockito.mock(RoutineScheduleRepositoryPort.class);RoutineGenerationService generation=org.mockito.Mockito.mock(RoutineGenerationService.class);RoutineSchedule schedule=RoutineSchedule.createWithoutNotification(1L,now,now.minusSeconds(60));given(schedules.findDueActive(now)).willReturn(List.of(schedule));given(schedules.save(any())).willAnswer(i->i.getArgument(0));
        new RoutineGenerationScheduler(schedules,generation,new RoutineGenerationScheduleCalculator(),Clock.fixed(now,ZoneOffset.UTC)).generateDue();
        then(generation).should().generate(1L,LocalDate.of(2026,8,16),RoutineGenerationType.SCHEDULED_DAILY,null);
    }
}
