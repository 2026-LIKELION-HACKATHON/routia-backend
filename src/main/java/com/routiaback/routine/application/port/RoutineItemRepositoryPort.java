package com.routiaback.routine.application.port;

import com.routiaback.routine.domain.RoutineItem;
import java.util.List;
import java.util.Optional;

public interface RoutineItemRepositoryPort {
    List<RoutineItem> findAllByRoutineIdOrderBySortOrder(Long routineId);
    Optional<RoutineItem> findItemById(Long itemId);
    RoutineItem save(RoutineItem item);
    List<RoutineItem> findAllByRoutineIds(List<Long> routineIds);
    List<RoutineItem> saveAll(List<RoutineItem> items);
}
