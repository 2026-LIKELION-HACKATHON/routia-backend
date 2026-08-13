package com.routiaback.routine.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

interface DailyRoutineJpaRepository extends JpaRepository<DailyRoutineJpaEntity, Long> {
    Optional<DailyRoutineJpaEntity> findByUserIdAndRoutineDate(Long userId, LocalDate routineDate);
}