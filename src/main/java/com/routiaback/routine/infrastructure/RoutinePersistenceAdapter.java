package com.routiaback.routine.infrastructure;

import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineItem;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
class RoutinePersistenceAdapter implements DailyRoutineRepositoryPort, RoutineItemRepositoryPort {

    private final DailyRoutineJpaRepository dailyRoutineJpaRepository;
    private final RoutineItemJpaRepository routineItemJpaRepository;

    RoutinePersistenceAdapter(DailyRoutineJpaRepository dailyRoutineJpaRepository,
                              RoutineItemJpaRepository routineItemJpaRepository) {
        this.dailyRoutineJpaRepository = dailyRoutineJpaRepository;
        this.routineItemJpaRepository = routineItemJpaRepository;
    }

    @Override
    public Optional<DailyRoutine> findByUserIdAndRoutineDate(Long userId, LocalDate routineDate) {
        return dailyRoutineJpaRepository.findByUserIdAndRoutineDate(userId, routineDate)
                .map(e -> new DailyRoutine(e.getId(), e.getUserId(), e.getRoutineDate(), e.getStatus(),
                        e.getDirectionText(), e.getHomeComment(), e.getCreatedAt(), e.getUpdatedAt()));
    }

    @Override
    public List<RoutineItem> findAllByRoutineIdOrderBySortOrder(Long routineId) {
        return routineItemJpaRepository.findAllByRoutineIdOrderBySortOrderAsc(routineId)
                .stream()
                .map(e -> new RoutineItem(e.getId(), e.getRoutineId(), e.getTimeSlot(), e.getCategory(),
                        e.getTitle(), e.getDetail(), e.getSortOrder(), e.isCompleted(), e.getCompletedAt(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .toList();
    }
}