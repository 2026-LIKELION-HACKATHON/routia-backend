package com.routiaback.routine.application.port;

import com.routiaback.routine.domain.DailyRoutine;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyRoutineRepositoryPort {
    Optional<DailyRoutine> findByUserIdAndRoutineDate(Long userId, LocalDate routineDate);
    Optional<DailyRoutine> findById(Long id);
}