package com.routiaback.notification.application.port;

import com.routiaback.notification.domain.RoutineSchedule;
import java.util.Optional;
import java.time.Instant;
import java.util.List;

public interface RoutineScheduleRepositoryPort {

    Optional<RoutineSchedule> findByUserId(Long userId);

    RoutineSchedule save(RoutineSchedule schedule);
    List<RoutineSchedule> findDueActive(Instant now);
}
