package com.routiaback.routine.application.port;

import com.routiaback.routine.domain.DailyRoutine;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DailyRoutineRepositoryPort {
    Optional<DailyRoutine> findByUserIdAndRoutineDate(Long userId, LocalDate routineDate);
    Optional<DailyRoutine> findById(Long id);
    List<DailyRoutine> findAllByUserIdAndRoutineDateBetween(Long userId, LocalDate start, LocalDate end);
    List<DailyRoutine> findReadyDueForNotification(Instant now);
    DailyRoutine save(DailyRoutine routine);
}
