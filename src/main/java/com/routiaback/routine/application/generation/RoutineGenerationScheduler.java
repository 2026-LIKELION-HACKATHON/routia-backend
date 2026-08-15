package com.routiaback.routine.application.generation;

import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.*;
import com.routiaback.routine.domain.RoutineGenerationType;
import java.time.*;
import org.slf4j.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component @EnableScheduling
public class RoutineGenerationScheduler {
    private static final Logger log=LoggerFactory.getLogger(RoutineGenerationScheduler.class);
    private final RoutineScheduleRepositoryPort schedules; private final RoutineGenerationService generation;
    private final RoutineGenerationScheduleCalculator calculator; private final Clock clock;
    public RoutineGenerationScheduler(RoutineScheduleRepositoryPort schedules,RoutineGenerationService generation,RoutineGenerationScheduleCalculator calculator,Clock clock){this.schedules=schedules;this.generation=generation;this.calculator=calculator;this.clock=clock;}
    @Scheduled(fixedDelayString="${routia.routine-generation.scheduler-delay-ms:60000}")
    public void generateDue(){Instant now=clock.instant();for(RoutineSchedule schedule:schedules.findDueActive(now)){RoutineGenerationScheduleCalculator.ScheduledTarget target=calculator.current(schedule);try{generation.generate(schedule.userId(),target.routineDate(),RoutineGenerationType.SCHEDULED_DAILY,target.notificationAt());}catch(RuntimeException ex){log.warn("Scheduled routine generation failed. userId={} routineDate={} error={}",schedule.userId(),target.routineDate(),ex.getClass().getSimpleName());}finally{RoutineGenerationScheduleCalculator.ScheduledTarget next=calculator.next(schedule.notificationTime(),schedule.timezone(),target.notificationAt());schedules.save(schedule.advance(next.generationAt(),clock.instant()));}}}
}
