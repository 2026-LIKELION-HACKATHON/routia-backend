package com.routiaback.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

interface RoutineScheduleJpaRepository extends JpaRepository<RoutineScheduleJpaEntity, Long> {
    List<RoutineScheduleJpaEntity> findAllByActiveTrueAndNextGenerationAtLessThanEqual(Instant now);
}
