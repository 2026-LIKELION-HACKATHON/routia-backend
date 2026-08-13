package com.routiaback.routine.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface RoutineItemJpaRepository extends JpaRepository<RoutineItemJpaEntity, Long> {
    List<RoutineItemJpaEntity> findAllByRoutineIdOrderBySortOrderAsc(Long routineId);
}