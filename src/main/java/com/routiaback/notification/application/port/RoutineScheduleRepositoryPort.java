package com.routiaback.notification.application.port;

import com.routiaback.notification.domain.RoutineSchedule;
import java.util.Optional;

public interface RoutineScheduleRepositoryPort {

    Optional<RoutineSchedule> findByUserId(Long userId);

    RoutineSchedule save(RoutineSchedule schedule);
}
